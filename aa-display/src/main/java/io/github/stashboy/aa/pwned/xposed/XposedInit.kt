package io.github.stashboy.aa.pwned.xposed

import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.Log.logexIfThrow
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.stashboy.aa.pwned.BuildConfig
import io.github.stashboy.aa.pwned.xposed.hook.AndroidAuoHook
import io.github.stashboy.aa.pwned.xposed.hook.AndroidHook
import io.github.stashboy.aa.pwned.xposed.hook.BaseHook
import io.github.stashboy.aa.pwned.xposed.hook.OtherHook

class XposedInit : IXposedHookZygoteInit, IXposedHookLoadPackage{
    companion object {
        const val TAG = "AADisplay_XposedInit"
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        val packageName = lpparam.packageName
        when{
            packageName == "android" && lpparam.appInfo == null -> arrayOf(AndroidHook)
            packageName == "com.google.android.projection.gearhead" -> arrayOf(AndroidAuoHook)
            packageName == BuildConfig.APPLICATION_ID || lpparam.appInfo == null || lpparam.appInfo.uid == 1000 -> null
            else -> arrayOf(OtherHook)
        }?.also {
            initHooks(lpparam, *it)
        }
    }

    private fun initHooks(lpparam: XC_LoadPackage.LoadPackageParam, vararg hook: BaseHook) {
        EzXHelperInit.initHandleLoadPackage(lpparam)
        hook.forEach {
            runCatching {
                if (it.isInit) return@forEach
                it.init(lpparam)
                it.isInit = true
                log(TAG, "Inited hook: ${it.tagName}")
            }.logexIfThrow("Failed init hook: ${it.tagName}")
        }
    }
}


