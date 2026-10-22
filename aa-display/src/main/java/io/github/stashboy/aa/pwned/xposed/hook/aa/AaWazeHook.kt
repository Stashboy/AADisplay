package io.github.stashboy.aa.pwned.xposed.hook.aa

import android.content.ContentProvider
import android.content.SharedPreferences
import android.content.pm.ResolveInfo
import android.database.MatrixCursor
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.loadClassOrNull
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.stashboy.aa.pwned.BuildConfig
import io.github.stashboy.aa.pwned.util.AADisplayConfig
import io.github.stashboy.aa.pwned.xposed.hook.AaHook
import io.github.stashboy.aa.pwned.xposed.log

/**
 * "Disable Waze on Android Auto" -- implemented entirely inside the Android Auto (gearhead) process.
 *
 * When the toggle is on, Waze is made to behave as if it is NOT connected to a car, so it keeps its
 * map on the phone instead of projecting to the head unit, and it is also hidden from Android Auto's
 * app list. Two gearhead-side hooks, both gated by the toggle and scoped to `com.waze` only:
 *
 *  1. CAR-CONNECTION INTERCEPT (the important one): gearhead hosts the `androidx.car.app.connection`
 *     provider (`...projectionstate.ProjectionStateProvider`). When Waze queries it for the
 *     connection state, return `CarConnectionState = 0` (NOT CONNECTED). Waze then runs normally on
 *     the phone. Every other app still sees the real state.
 *
 *  2. APP-LIST FILTER: drop `com.waze` from PackageManager.queryIntentServices/queryIntentActivities
 *     results so Waze does not appear in Android Auto's launcher. Guarded to only rewrite a
 *     multi-resolver result and never return an empty list, so a caller that dereferences the first
 *     result cannot crash.
 *
 * Uses only stable framework APIs (ContentProvider, PackageManager, the androidx CarConnection
 * contract) plus the constant `com.waze` package name -- no Waze offsets, nothing in Waze's process
 * -- so Waze updates never affect it. The toggle is a pure live config read (no pm-disable, no global
 * settings, no persistent device state).
 */
object AaWazeHook : AaHook() {
    override val tagName: String = "AAD_AaWazeHook"

    private const val WAZE_PACKAGE = "com.waze"
    private const val CAR_CONNECTION_STATE = "CarConnectionState"
    private const val NOT_CONNECTED = 0
    private const val PROJECTION_STATE_PROVIDER =
        "com.google.android.apps.auto.components.projectionstate.ProjectionStateProvider"

    // Runs in all gearhead processes; each hook only installs where its target class exists.
    override fun isSupportProcess(processName: String): Boolean = true

    override fun hook(config: SharedPreferences?, lpparam: XC_LoadPackage.LoadPackageParam) {
        val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID, AADisplayConfig.ConfigName)

        // (1) Tell Waze (only) it is not connected to a car, so it stays on the phone.
        try {
            val providerClass = loadClassOrNull(PROJECTION_STATE_PROVIDER)
            if (providerClass != null) {
                findAllMethods(providerClass) { name == "query" }.forEach { method ->
                    method.hookBefore { param ->
                        if (!isDisableEnabled(prefs)) return@hookBefore
                        val caller = try {
                            (param.thisObject as? ContentProvider)?.callingPackage
                        } catch (e: Throwable) {
                            null
                        }
                        if (caller != WAZE_PACKAGE) return@hookBefore
                        @Suppress("UNCHECKED_CAST")
                        val projection = param.args.firstOrNull {
                            it is Array<*> && (it as Array<*>).all { x -> x == null || x is String }
                        } as? Array<String>
                        val columns = projection?.takeIf { it.isNotEmpty() } ?: arrayOf(CAR_CONNECTION_STATE)
                        val row = Array<Any?>(columns.size) { i ->
                            if (columns[i] == CAR_CONNECTION_STATE) NOT_CONNECTED else null
                        }
                        // Setting result in hookBefore skips the real query -> Waze sees "not connected".
                        param.result = MatrixCursor(columns, 1).apply { addRow(row) }
                        log(tagName, "told Waze it is not connected (CarConnectionState=$NOT_CONNECTED)")
                    }
                }
            } else {
                log(tagName, "ProjectionStateProvider not found in ${lpparam.processName}; connection intercept skipped")
            }
        } catch (e: Throwable) {
            log(tagName, "connection-intercept install failed", e)
        }

        // (2) Hide Waze from Android Auto's app discovery.
        try {
            val pmClass = loadClassOrNull("android.app.ApplicationPackageManager") ?: return
            for (methodName in arrayOf("queryIntentServices", "queryIntentActivities")) {
                findAllMethods(pmClass) { name == methodName }.forEach { method ->
                    method.hookAfter { param ->
                        try {
                            if (!isDisableEnabled(prefs)) return@hookAfter
                            val result = param.result as? List<*> ?: return@hookAfter
                            if (result.isEmpty()) return@hookAfter
                            val filtered = result.filterNot { it is ResolveInfo && it.isWaze() }
                            // Only rewrite when Waze was actually removed AND something remains.
                            // Never hand back an empty list: if Waze were the sole resolver, a caller
                            // doing queryIntent...().first() would throw inside gearhead (which could
                            // drop the whole AA session). The app-list query always returns several
                            // apps, so Waze is still hidden there; a Waze-only query is left untouched.
                            if (filtered.isNotEmpty() && filtered.size != result.size) {
                                log(tagName, "hid Waze from $methodName: ${result.size} -> ${filtered.size}")
                                param.result = ArrayList(filtered)
                            }
                        } catch (e: Throwable) {
                            log(tagName, "app-list-filter callback error", e)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            log(tagName, "app-list-filter install failed", e)
        }
    }

    private fun ResolveInfo.isWaze(): Boolean =
        serviceInfo?.packageName == WAZE_PACKAGE ||
        activityInfo?.packageName == WAZE_PACKAGE ||
        providerInfo?.packageName == WAZE_PACKAGE

    private fun isDisableEnabled(prefs: XSharedPreferences): Boolean = try {
        prefs.reload()
        AADisplayConfig.DisableWazeOnAa.get(prefs)
    } catch (e: Throwable) {
        false
    }
}
