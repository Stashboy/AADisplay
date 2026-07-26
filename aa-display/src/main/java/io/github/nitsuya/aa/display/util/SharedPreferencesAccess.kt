package io.github.nitsuya.aa.display.util

import android.content.Context
import android.content.SharedPreferences
import java.io.File

object SharedPreferencesAccess {
    private const val HookPreferencesReadyVersionKey = "__AADisplayHookPreferencesReadyVersion"

    fun openForHooks(context: Context, name: String): SharedPreferences {
        return try {
            @Suppress("DEPRECATION")
            context.getSharedPreferences(name, Context.MODE_WORLD_READABLE)
        } catch (_: SecurityException) {
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
        }
    }

    fun makeReadableForHooks(context: Context, name: String): Boolean {
        return try {
            @Suppress("DEPRECATION")
            context.getSharedPreferences(name, Context.MODE_WORLD_READABLE)
                .edit()
                .putInt(HookPreferencesReadyVersionKey, 1)
                .commit()
        } catch (_: SecurityException) {
            makePrivatePreferencesReadable(context, name)
        }
    }

    private fun makePrivatePreferencesReadable(context: Context, name: String): Boolean {
        val dataDir = File(context.applicationInfo.dataDir)
        val prefsDir = File(dataDir, "shared_prefs")
        val prefsFile = File(prefsDir, "$name.xml")

        var ok = ensureExecutable(dataDir)
        if (prefsDir.exists()) {
            ok = ensureExecutable(prefsDir) && ok
        }
        if (prefsFile.exists()) {
            ok = ensureReadable(prefsFile) && ok
        }
        return ok
    }

    private fun ensureExecutable(file: File): Boolean {
        return file.exists() && (file.setExecutable(true, false) || file.canExecute())
    }

    private fun ensureReadable(file: File): Boolean {
        return file.exists() && (file.setReadable(true, false) || file.canRead())
    }
}
