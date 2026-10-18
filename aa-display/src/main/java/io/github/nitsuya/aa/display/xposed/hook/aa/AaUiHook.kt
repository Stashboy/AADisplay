package io.github.nitsuya.aa.display.xposed.hook.aa

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.car.Car
import android.support.car.CarConnectionCallback
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.kyuubiran.ezxhelper.init.InitFields
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.getIdByName
import com.github.kyuubiran.ezxhelper.utils.getObjectOrNull
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.loadClass
import de.robv.android.xposed.callbacks.XC_LoadPackage
import de.robv.android.xposed.callbacks.XCallback
import io.github.nitsuya.aa.display.BuildConfig
import io.github.nitsuya.aa.display.R
import io.github.nitsuya.aa.display.service.AaActivityService
import io.github.nitsuya.aa.display.util.AABroadcastConst
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.util.getGmsCarFirstPartyManager
import io.github.nitsuya.aa.display.util.startCarAaDisplay
import io.github.nitsuya.aa.display.xposed.hook.AaHook
import io.github.nitsuya.aa.display.xposed.hook.abortMethod
import io.github.nitsuya.aa.display.xposed.log
import io.github.nitsuya.template.bases.runMain
import io.github.qauxv.ui.CommonContextWrapper
import kotlinx.coroutines.delay
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Constructor

object AaUiHook: AaHook() {
    override val tagName: String = "AAD_AaUiHook"

    private lateinit var layoutInfoConstructor: Constructor<*>

    private var resLayoutLeftResourceId: Int = 0
    private var resLayoutRightResourceId: Int = 0

    private var resLayoutGhFacetBarId: Int = 0
    private var resIdStatusBarId: Int = 0
    private var resIdLauncherAndDashboardIconContainerId: Int = 0
    private var resIdLauncherAndDashboardIconId: Int = 0
    private var resIdAssistantIconContainerId: Int = 0
    private var canHookLayout: Boolean = false
    private var canHookFacetBar: Boolean = false

    override fun isSupportProcess(processName: String): Boolean {
        return processProjection == processName
    }

    override fun loadDexClass(bridge: DexKitBridge, lpparam: XC_LoadPackage.LoadPackageParam) {
        val classes = bridge.findClass {
            searchPackages = listOf("")
            matcher {
                usingStrings {
                    add(
                        "LayoutInfo{layoutResourceId=",
                        StringMatchType.StartsWith,
                        false
                    )
                }
            }
        }
        if (classes.isEmpty() || classes.size > 1) {
            throw NoSuchMethodException("AaUiHook: not found LayoutInfo class：${classes.size}")
        }
        layoutInfoConstructor = resolveLayoutInfoConstructor(classes[0].name)

        resLayoutGhFacetBarId = InitFields.appContext.resources.getIdentifier("gh_coolwalk_vertical_facet_bar", "layout", InitFields.appContext.packageName)
        resIdStatusBarId = getIdByName("status_bar")
        resIdLauncherAndDashboardIconContainerId = getIdByName("launcher_and_dashboard_icon_container")
        resIdLauncherAndDashboardIconId = getIdByName("launcher_and_dashboard_icon")
        resIdAssistantIconContainerId = getIdByName("assistant_icon_container")

        resLayoutLeftResourceId = InitFields.appContext.resources.getIdentifier("sys_ui_layout_canonical_vertical_rail_lhd", "layout", InitFields.appContext.packageName)
        resLayoutRightResourceId = InitFields.appContext.resources.getIdentifier("sys_ui_layout_canonical_vertical_rail_rhd", "layout", InitFields.appContext.packageName)

        canHookLayout = resLayoutLeftResourceId != 0 && resLayoutRightResourceId != 0
        if (!canHookLayout) {
            log(
                tagName,
                "AaUiHook: skip layout override, missing canonical layout resources: lhd=$resLayoutLeftResourceId, rhd=$resLayoutRightResourceId"
            )
        }
        // Surgical mic->Home swap only needs the facet-bar layout + the assistant (mic) container.
        // The launcher icon is used by the optional closeLauncherDashboard behavior; it is not
        // required for the swap, so it is not part of the gate.
        canHookFacetBar =
            resLayoutGhFacetBarId != 0 &&
            resIdAssistantIconContainerId != 0
        if (!canHookFacetBar) {
            log(
                tagName,
                "AaUiHook: skip facet-bar override, missing resources: facet=$resLayoutGhFacetBarId, assistantContainer=$resIdAssistantIconContainerId, status=$resIdStatusBarId, launcherContainer=$resIdLauncherAndDashboardIconContainerId, launcherIcon=$resIdLauncherAndDashboardIconId"
            )
        }
    }

    override fun hook(config: SharedPreferences?, lpparam: XC_LoadPackage.LoadPackageParam) {
        log(tagName,  "AaUiHook: ~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        hookBaseClick()
        if (canHookLayout) {
            hookLayout()
        }
        if (canHookFacetBar) {
            hookFacetBar(config)
        }
        hookRadius(config)
    }

    private fun hookLayout() {
        layoutInfoConstructor.hookAfter { param -> log(tagName, param.thisObject.toString()) }
        layoutInfoConstructor.hookBefore { param ->
            if (param.args.size < 5) return@hookBefore
            val layoutTypeCode = layoutTypeCode(param.args[3] ?: return@hookBefore) ?: return@hookBefore
            // Skip cluster/auxiliary layouts to avoid overriding non-main surfaces.
            if (layoutTypeCode in setOf(7, 8, 9)) return@hookBefore
            val isRightHandDrive = (param.args[4] as? Boolean) == true
            param.args[0] = if (isRightHandDrive) resLayoutRightResourceId else resLayoutLeftResourceId
            // Force canonical vertical rail for LHD/RHD.
            setLayoutTypeArg(param.args, if (isRightHandDrive) 3 else 2)
            if (param.args.size > 5 && param.args[5] is Boolean) {
                param.args[5] = true //hasVerticalRail
            }
        }
    }

    private fun autoOpenAaDisplay(context: Context) {
        // AA 17.8 removed com.google.android.projection.gearhead.service.CarSystemUiControllerService,
        // which the facet-bar auto-open previously used. Launch through the aauto-SDK first-party car
        // client instead (the same proven path AaMainFragment uses), so auto-open is independent of
        // obfuscated gearhead internals and survives future Android Auto renames. Fully guarded: any
        // failure (incl. an unavailable manager) is a silent no-op and never disturbs the live display.
        try {
            val car = Car.createCar(context, object : CarConnectionCallback() {
                override fun onConnected(connectedCar: Car) {
                    try {
                        connectedCar.getGmsCarFirstPartyManager().startCarAaDisplay()
                        log(tagName, "autoOpen: startCarAaDisplay invoked")
                    } catch (e: Throwable) {
                        log(tagName, "autoOpen: startCarAaDisplay failed", e)
                    } finally {
                        runCatching { connectedCar.disconnect() }
                    }
                }

                override fun onDisconnected(disconnectedCar: Car) {}
            })
            car.connect()
        } catch (e: Throwable) {
            log(tagName, "autoOpen: car connect failed", e)
        }
    }

    private fun resolveLayoutInfoConstructor(className: String): Constructor<*> {
        // Keep only stable shape checks so this works across AA 16.4 and 16.6+.
        val clazz = loadClass(className)
        val fallback = clazz.declaredConstructors.firstOrNull { ctor ->
            val p = ctor.parameterTypes
            p.size >= 10
                && p[0] == Int::class.javaPrimitiveType
                && p[1] == Int::class.javaPrimitiveType
                && p[2] == Int::class.javaPrimitiveType
                && p[4] == Boolean::class.javaPrimitiveType
                && p[5] == Boolean::class.javaPrimitiveType
                && p[7] == Boolean::class.javaPrimitiveType
                && p[8] == Boolean::class.javaPrimitiveType
                && p[9] == Boolean::class.javaPrimitiveType
                && !p[6].isPrimitive
                && (p[3] == Int::class.javaPrimitiveType || p[3].isEnum)
        } ?: throw NoSuchMethodException("AaUiHook: not found compatible LayoutInfo constructor for $className")

        fallback.isAccessible = true
        log(tagName, "AaUiHook: fallback constructor selected, paramCount=${fallback.parameterCount}, layoutTypeArg=${fallback.parameterTypes[3].name}")
        return fallback
    }

    private fun layoutTypeCode(raw: Any): Int? {
        return when (raw) {
            is Int -> raw - 1
            is Enum<*> -> raw.toString().toIntOrNull()
            else -> raw.toString().toIntOrNull()
        }
    }

    private fun setLayoutTypeArg(args: Array<Any?>, targetCode: Int) {
        val raw = args.getOrNull(3) ?: return
        when (raw) {
            is Int -> args[3] = targetCode + 1
            is Enum<*> -> {
                val enumClass = raw.javaClass
                val enumValue = enumClass.enumConstants?.firstOrNull { c ->
                    c?.toString()?.toIntOrNull() == targetCode
                }
                if (enumValue != null) {
                    args[3] = enumValue
                }
            }
        }
    }

    private fun hookFacetBar(config: SharedPreferences?) {
        val closeLauncherDashboard = AADisplayConfig.CloseLauncherDashboard.get(config)
        val autoOpen = AADisplayConfig.AutoOpen.get(config)
        findMethod(LayoutInflater::class.java) {
            name == "inflate"
            && parameterCount == 3
            && parameterTypes[0] == Int::class.javaPrimitiveType // resource
            && parameterTypes[1] == ViewGroup::class.java // root
            && parameterTypes[2] == Boolean::class.javaPrimitiveType // attachToRoot
        }.hookAfter { param ->
            if (param.args[0] as Int != resLayoutGhFacetBarId) {
                return@hookAfter
            }
            val facetBar = param.result as ViewGroup? ?: return@hookAfter //androidx.constraintlayout.widget.ConstraintLayout
            val ctx = (param.thisObject as LayoutInflater).context
            // Surgical swap: keep Android Auto's NATIVE facet bar exactly as inflated (recents,
            // AA's own Home, the hotseat app icons, status bar). Replace ONLY the assistant (mic)
            // slot with the AADisplay Home button. Back + Fullscreen stay on the phone-side
            // DisplayWindow controls (left untouched). param.result is returned unchanged so the
            // native layout/constraints are preserved.
            try {
                if (closeLauncherDashboard && resIdLauncherAndDashboardIconId != 0) {
                    facetBar.findViewById<View>(resIdLauncherAndDashboardIconId)?.apply {
                        setOnClickFinallyListener {
                            performLongClick()
                        }
                    }
                }
                val assistantContainer = facetBar.findViewById<ViewGroup>(resIdAssistantIconContainerId)
                if (assistantContainer != null) {
                    // COVER, don't fight. AA re-shows the mic after inflate, so instead of toggling
                    // its visibility (a race that either loses or needs a frame-flooding listener),
                    // overlay ONE opaque Home button on top and never touch the bar again:
                    //  - built with CommonContextWrapper so the module drawable (reserved package id
                    //    0x64) resolves in the gearhead process instead of rendering blank;
                    //  - opaque background matching the black rail => the mic underneath is fully
                    //    hidden regardless of what AA does with it;
                    //  - translationZ keeps it top-most for both drawing and touch, no listener.
                    // Result: zero ongoing render cost (nothing re-runs per frame).
                    val moduleCtx = CommonContextWrapper.createAppCompatContext(ctx)
                    val homeBtn = ImageView(moduleCtx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = ImageView.ScaleType.CENTER_INSIDE
                        setImageResource(R.drawable.ic_aa_home_44)
                        setPadding(0, 5, 0, 5)
                        setBackgroundColor(android.graphics.Color.BLACK)
                        isClickable = true
                        isFocusable = true
                        translationZ = 100f
                        setOnClickListener {
                            ctx.sendBroadcast(Intent().apply {
                                action = AABroadcastConst.ACTION_SCREEN_CONTROL
                                putExtra(AABroadcastConst.EXTRA_ACTION, KeyEvent.KEYCODE_HOME)
                            })
                        }
                    }
                    assistantContainer.addView(homeBtn)
                    homeBtn.bringToFront()
                } else {
                    log(tagName, "AaUiHook: assistant_icon_container not found at runtime; mic not replaced")
                }
                if (autoOpen) {
                    facetBar.post {
                        runMain {
                            delay(1000)
                            autoOpenAaDisplay(ctx)
                        }
                    }
                }
            } catch (e: Throwable) {
                log(tagName, "AaUiHook: facet-bar mic->Home swap failed", e)
            }
        }
    }

    private fun hookBaseClick() {
        try {
            findMethod(View::class.java) {
                name == "setOnLongClickListener"
                && parameterCount == 1
                && parameterTypes[0] == View.OnLongClickListener::class.java
            }.hookBefore(XCallback.PRIORITY_LOWEST) {
                if (it.args[0] is FinallyListener) return@hookBefore
                val view = it.thisObject as View
                if (!view.hasOnLongClickListeners() || (view.getObjectOrNull("mListenerInfo")?.getObjectOrNull("mOnLongClickListener") is FinallyListener).not()) {
                    return@hookBefore
                }
                view.setOnOriLongClickListener(it.args[0] as View.OnLongClickListener)
                it.abortMethod()
            }
        } catch (e: Throwable) {
            log(tagName, "hook View.setOnLongClickListener", e)
        }
        try {
            findMethod(View::class.java) {
                name == "setOnClickListener"
                && parameterCount == 1
                && parameterTypes[0] == View.OnClickListener::class.java
            }.hookBefore(XCallback.PRIORITY_LOWEST) {
                if (it.args[0] is FinallyListener) return@hookBefore
                val view = it.thisObject as View
                if (!view.hasOnClickListeners() || (view.getObjectOrNull("mListenerInfo")?.getObjectOrNull("mOnClickListener") is FinallyListener).not()) {
                    return@hookBefore
                }
                view.setOnOriClickListener(it.args[0] as View.OnClickListener)
                it.abortMethod()
            }
        } catch (e: Throwable) {
            log(tagName, "hook View.setOnClickListener", e)
        }
    }

    private fun hookRadius(config: SharedPreferences?) {
        if(!AADisplayConfig.ForceRightAngle.get(config)){
            return
        }
        try{
            val targetClass = loadClass("com.google.android.gms.car.ProjectionWindowDecorationParams")
            val ctor = targetClass.declaredConstructors.firstOrNull { c ->
                val p = c.parameterTypes
                p.size >= 9 &&
                    p[0] == Int::class.javaPrimitiveType &&
                    p[1] == Int::class.javaPrimitiveType &&
                    p[2] == Int::class.javaPrimitiveType &&
                    p[3] == Int::class.javaPrimitiveType &&
                    p[4] == Int::class.javaPrimitiveType &&
                    p[5] == Int::class.javaPrimitiveType &&
                    p[6] == Int::class.javaPrimitiveType &&
                    p[7] == Boolean::class.javaPrimitiveType &&
                    p[8] == Boolean::class.javaPrimitiveType
            } ?: throw NoSuchMethodException("AaUiHook: ProjectionWindowDecorationParams compatible constructor not found")
            ctor.isAccessible = true
            ctor.hookBefore { param ->
                if (param.args.size > 5 && param.args[5] is Int) {
                    param.args[5] = 0
                }
            }
        } catch (e: Throwable) {
            log(tagName, "ProjectionWindowDecorationParams", e)
        }
    }

    interface FinallyListener
    private fun interface OnClickFinallyListener: View.OnClickListener, FinallyListener
    private fun interface OnLongClickFinallyListener: View.OnLongClickListener, FinallyListener
    private fun View.setOnClickFinallyListener(l: OnClickFinallyListener) = this.setOnClickListener(l)
    private fun View.setOnLongClickFinallyListener(l: OnLongClickFinallyListener) = this.setOnLongClickListener(l)
    private fun View.setOnOriClickListener(l: View.OnClickListener) = this.setTag(R.id.ori_click_listener, l)
    private fun View.setOnOriLongClickListener(l: View.OnLongClickListener) = this.setTag(R.id.ori_long_click_listener, l)
    private fun View.performOriClick() {
        val clickListener = this.getTag(R.id.ori_click_listener) as View.OnClickListener? ?: return
        clickListener.onClick(this)
    }
    private fun View.performOriLongClick(): Boolean {
        val clickListener = this.getTag(R.id.ori_long_click_listener) as View.OnLongClickListener? ?: return false
        return clickListener.onLongClick(this)
    }
}
