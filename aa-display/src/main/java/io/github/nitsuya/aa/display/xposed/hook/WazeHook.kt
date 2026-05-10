package io.github.nitsuya.aa.display.xposed.hook

import android.content.ContentProviderClient
import android.content.ContentResolver
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.nitsuya.aa.display.BuildConfig
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.util.WazeOnAaManager

object WazeHook : BaseHook() {
    override val tagName: String = "AAD_WazeHook"

    private const val CAR_CONNECTION_AUTHORITY = "androidx.car.app.connection"
    private const val CAR_CONNECTION_STATE = "CarConnectionState"

    override fun init(lpparam: XC_LoadPackage.LoadPackageParam) {
        val configPreferences = XSharedPreferences(BuildConfig.APPLICATION_ID, AADisplayConfig.ConfigName)
        hookContentResolverQueries(configPreferences)
        hookContentResolverCalls(configPreferences)
        hookContentProviderClientQueries(configPreferences)
        hookContentProviderClientCalls(configPreferences)
    }

    private fun hookContentResolverQueries(configPreferences: XSharedPreferences) {
        hookMethodsByName(ContentResolver::class.java, "query") { param ->
            val uri = extractUri(param.args) ?: return@hookMethodsByName
            if (!shouldIntercept(uri, configPreferences, param.thisObject as? ContentResolver)) return@hookMethodsByName
            param.result = buildNotConnectedCursor(extractProjection(param.args))
        }
    }

    private fun hookContentResolverCalls(configPreferences: XSharedPreferences) {
        hookMethodsByName(ContentResolver::class.java, "call") { param ->
            val authority = extractCallAuthority(param.args) ?: return@hookMethodsByName
            if (authority != CAR_CONNECTION_AUTHORITY) return@hookMethodsByName
            if (!isDisableEnabled(configPreferences, param.thisObject as? ContentResolver)) return@hookMethodsByName
            param.result = Bundle().apply {
                putInt(CAR_CONNECTION_STATE, 0)
            }
        }
    }

    private fun hookContentProviderClientQueries(configPreferences: XSharedPreferences) {
        hookMethodsByName(ContentProviderClient::class.java, "query") { param ->
            val uri = extractUri(param.args) ?: return@hookMethodsByName
            if (!shouldIntercept(uri, configPreferences, null)) return@hookMethodsByName
            param.result = buildNotConnectedCursor(extractProjection(param.args))
        }
    }

    private fun hookContentProviderClientCalls(configPreferences: XSharedPreferences) {
        hookMethodsByName(ContentProviderClient::class.java, "call") { param ->
            val authority = extractCallAuthority(param.args) ?: return@hookMethodsByName
            if (authority != CAR_CONNECTION_AUTHORITY) return@hookMethodsByName
            if (!isDisableEnabled(configPreferences, null)) return@hookMethodsByName
            param.result = Bundle().apply {
                putInt(CAR_CONNECTION_STATE, 0)
            }
        }
    }

    private fun hookMethodsByName(
        targetClass: Class<*>,
        methodName: String,
        beforeHookedMethod: (XC_MethodHook.MethodHookParam) -> Unit
    ) {
        targetClass.declaredMethods
            .filter { method -> method.name == methodName }
            .forEach { method ->
                XposedBridge.hookMethod(method, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        beforeHookedMethod(param)
                    }
                })
            }
    }

    private fun shouldIntercept(
        uri: Uri,
        configPreferences: XSharedPreferences,
        contentResolver: ContentResolver?
    ): Boolean {
        if (uri.authority != CAR_CONNECTION_AUTHORITY) return false
        return isDisableEnabled(configPreferences, contentResolver)
    }

    private fun isDisableEnabled(
        configPreferences: XSharedPreferences,
        contentResolver: ContentResolver?
    ): Boolean {
        if (contentResolver != null) {
            val globalValue = runCatching {
                Settings.Global.getInt(
                    contentResolver,
                    WazeOnAaManager.GLOBAL_DISABLE_WAZE_ON_AA_KEY,
                    -1
                )
            }.getOrNull()

            if (globalValue != null && globalValue >= 0) {
                return globalValue == 1
            }
        }

        configPreferences.reload()
        return AADisplayConfig.DisableWazeOnAa.get(configPreferences)
    }

    private fun extractUri(args: Array<Any?>): Uri? {
        return args.firstOrNull { it is Uri } as? Uri
    }

    private fun extractCallAuthority(args: Array<Any?>): String? {
        if (args.isEmpty()) return null
        val firstArg = args[0]
        return when (firstArg) {
            is Uri -> firstArg.authority
            is String -> firstArg
            else -> (args.firstOrNull { it is Uri } as? Uri)?.authority
        }
    }

    private fun extractProjection(args: Array<Any?>): Array<String>? {
        for (arg in args) {
            if (arg is Array<*> && arg.all { it == null || it is String }) {
                @Suppress("UNCHECKED_CAST")
                return arg as? Array<String>
            }
        }
        return null
    }

    private fun buildNotConnectedCursor(projection: Array<String>?): Cursor {
        val columns = projection?.takeIf { it.isNotEmpty() } ?: arrayOf(CAR_CONNECTION_STATE)
        val row = Array<Any?>(columns.size) { index ->
            if (columns[index] == CAR_CONNECTION_STATE) 0 else null
        }
        return MatrixCursor(columns, 1).apply {
            addRow(row)
        }
    }
}
