package io.github.stashboy.aa.pwned.xposed.hook.aa

import android.content.ContentProvider
import android.content.SharedPreferences
import android.database.MatrixCursor
import android.os.SystemClock
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
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
 * When the toggle is on, Waze is told it is NOT connected to a car, so it keeps its map on the phone
 * instead of auto-handing it to the head unit. Android Auto hosts the `androidx.car.app.connection`
 * provider (`...projectionstate.ProjectionStateProvider`); this hook intercepts that provider's
 * query() and, for calls from `com.waze` ONLY, returns `CarConnectionState = 0` (NOT CONNECTED).
 * Every other app still sees the real state.
 *
 * SCOPE NOTE: this does NOT hide Waze from the Android Auto launcher. Coolwalk builds its app grid
 * from a car-app registry enumerated by Google Play services (`com.google.android.gms`) -- a process
 * this module does not hook -- so Waze stays listed and can still be launched explicitly. An explicit
 * launch binds Waze to the car surface directly and is not governed by the connection signal, so Waze
 * then projects normally. (An earlier `queryIntentServices`/`queryIntentActivities` filter attempted
 * the hide but could not reach that registry; it was removed as it had no effect and added per-call
 * cost on hot Android Auto code paths.)
 *
 * Uses only stable framework APIs (ContentProvider + the androidx CarConnection contract) plus the
 * constant `com.waze` package name -- no Waze offsets, nothing in Waze's process -- so Waze updates
 * never affect it. The toggle is cached and refreshed at most once per second, so the hot
 * provider-query path does no per-call disk I/O.
 */
object AaWazeHook : AaHook() {
    override val tagName: String = "AAD_AaWazeHook"

    private const val WAZE_PACKAGE = "com.waze"
    private const val CAR_CONNECTION_STATE = "CarConnectionState"
    private const val NOT_CONNECTED = 0
    private const val CONFIG_REFRESH_MS = 1000L
    private const val PROJECTION_STATE_PROVIDER =
        "com.google.android.apps.auto.components.projectionstate.ProjectionStateProvider"

    private val prefs by lazy { XSharedPreferences(BuildConfig.APPLICATION_ID, AADisplayConfig.ConfigName) }

    @Volatile private var cachedEnabled = false
    @Volatile private var lastRefreshMs = 0L

    // Installs only in the gearhead process that hosts ProjectionStateProvider; no-ops elsewhere.
    override fun isSupportProcess(processName: String): Boolean = true

    override fun hook(config: SharedPreferences?, lpparam: XC_LoadPackage.LoadPackageParam) {
        // Seed the cached toggle from the snapshot AndroidAuoHook already reloaded, so the first
        // intercepted query has the right value without touching disk.
        cachedEnabled = try {
            config?.let { AADisplayConfig.DisableWazeOnAa.get(it) } ?: false
        } catch (e: Throwable) {
            false
        }
        lastRefreshMs = SystemClock.elapsedRealtime()

        try {
            val providerClass = loadClassOrNull(PROJECTION_STATE_PROVIDER)
            if (providerClass == null) {
                log(tagName, "ProjectionStateProvider not found in ${lpparam.processName}; Waze connection intercept not installed")
                return
            }
            var installed = 0
            findAllMethods(providerClass) { name == "query" }.forEach { method ->
                method.hookBefore { param ->
                    if (!isDisableEnabled()) return@hookBefore
                    val caller = try {
                        (param.thisObject as? ContentProvider)?.callingPackage
                    } catch (e: Throwable) {
                        null
                    }
                    if (caller != WAZE_PACKAGE) return@hookBefore
                    // The projection (requested columns) is the 2nd query() arg in both overloads.
                    @Suppress("UNCHECKED_CAST")
                    val projection = param.args.getOrNull(1) as? Array<String>
                    val columns = projection?.takeIf { it.isNotEmpty() } ?: arrayOf(CAR_CONNECTION_STATE)
                    val row = Array<Any?>(columns.size) { i ->
                        if (columns[i] == CAR_CONNECTION_STATE) NOT_CONNECTED else null
                    }
                    // Setting the result in a before-hook skips the real query -> Waze sees "not connected".
                    param.result = MatrixCursor(columns, 1).apply { addRow(row) }
                }
                installed++
            }
            log(tagName, "Waze connection intercept armed ($installed query overload(s)) in ${lpparam.processName}")
        } catch (e: Throwable) {
            log(tagName, "connection-intercept install failed", e)
        }
    }

    /** Toggle value, cached and refreshed at most once per [CONFIG_REFRESH_MS] to keep the hot path off disk. */
    private fun isDisableEnabled(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (now - lastRefreshMs >= CONFIG_REFRESH_MS) {
            lastRefreshMs = now
            cachedEnabled = try {
                prefs.reload()
                AADisplayConfig.DisableWazeOnAa.get(prefs)
            } catch (e: Throwable) {
                cachedEnabled
            }
        }
        return cachedEnabled
    }
}
