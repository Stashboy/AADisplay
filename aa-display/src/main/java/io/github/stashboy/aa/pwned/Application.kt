package io.github.stashboy.aa.pwned

import android.content.Context
import android.os.Process
import com.github.kyuubiran.ezxhelper.utils.tryOrNull
import com.google.android.material.color.DynamicColors
import com.topjohnwu.superuser.Shell
import io.github.stashboy.aa.pwned.util.AADisplayConfig
import io.github.stashboy.aa.pwned.util.SharedPreferencesAccess
import io.github.stashboy.aa.pwned.xposed.CoreManager
import io.github.stashboy.aa.pwned.xposed.CoreManagerService


val IsSystemEnv by lazy {
    Process.myUid() == 1000
}
val CoreApi by lazy {
    if(!IsSystemEnv) CoreManager
    else CoreManagerService.instance!!
}
lateinit var App : Application
class Application: android.app.Application() {
    init {
        App = this
        tryOrNull {
            Shell.setDefaultBuilder(Shell.Builder.create().setTimeout(30))
        }
    }

    override fun onCreate() {
        super.onCreate()
        SharedPreferencesAccess.makeReadableForHooks(this, AADisplayConfig.ConfigName)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
}
