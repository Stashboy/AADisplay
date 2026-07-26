package io.github.nitsuya.aa.display.xposed.hook.aa

import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.github.kyuubiran.ezxhelper.init.InitFields
import com.github.kyuubiran.ezxhelper.utils.argTypes
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.getIdByName
import com.github.kyuubiran.ezxhelper.utils.getObjectOrNull
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.loadClass
import com.github.kyuubiran.ezxhelper.utils.staticMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage
import de.robv.android.xposed.callbacks.XCallback
import io.github.nitsuya.aa.display.BuildConfig
import io.github.nitsuya.aa.display.R
import io.github.nitsuya.aa.display.service.AaActivityService
import io.github.nitsuya.aa.display.util.AABroadcastConst
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.xposed.hook.AaHook
import io.github.nitsuya.aa.display.xposed.hook.abortMethod
import io.github.nitsuya.aa.display.xposed.log
import io.github.nitsuya.template.bases.runMain
import io.github.qauxv.ui.CommonContextWrapper
import kotlinx.coroutines.delay
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.reflect.Constructor
import java.lang.reflect.Method

object AaUiHook: AaHook() {
    override val tagName: String = "AAD_AaUiHook"

    private lateinit var layoutInfoConstructor: Constructor<*>
    private var startMethod: Method? = null

    private var resLayoutLeftResourceId: Int = 0
    private var resLayoutRightResourceId: Int = 0

    private var resLayoutGhFacetBarId: Int = 0
    private var resIdStatusBarId: Int = 0
    private var resIdLauncherAndDashboardIconContainerId: Int = 0
    private var resIdLauncherAndDashboardIconId: Int = 0
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

        try{
            startMethod = loadClass("com.google.android.projection.gearhead.service.CarSystemUiControllerService").staticMethod("a", null, argTypes(Intent::class.java))
        } catch (e: Throwable){
            log(tagName,  "AaUiHook: not found CarSystemUiControllerService.a static method", e)
        }

        resLayoutGhFacetBarId = InitFields.appContext.resources.getIdentifier("gh_coolwalk_vertical_facet_bar", "layout", InitFields.appContext.packageName)
        resIdStatusBarId = getIdByName("status_bar")
        resIdLauncherAndDashboardIconContainerId = getIdByName("launcher_and_dashboard_icon_container")
        resIdLauncherAndDashboardIconId = getIdByName("launcher_and_dashboard_icon")

        resLayoutLeftResourceId = InitFields.appContext.resources.getIdentifier("sys_ui_layout_canonical_vertical_rail_lhd", "layout", InitFields.appContext.packageName)
        resLayoutRightResourceId = InitFields.appContext.resources.getIdentifier("sys_ui_layout_canonical_vertical_rail_rhd", "layout", InitFields.appContext.packageName)

        canHookLayout = resLayoutLeftResourceId != 0 && resLayoutRightResourceId != 0
        if (!canHookLayout) {
            log(
                tagName,
                "AaUiHook: skip layout override, missing canonical layout resources: lhd=$resLayoutLeftResourceId, rhd=$resLayoutRightResourceId"
            )
        }
        canHookFacetBar =
            resLayoutGhFacetBarId != 0 &&
            resIdStatusBarId != 0 &&
            resIdLauncherAndDashboardIconContainerId != 0 &&
            resIdLauncherAndDashboardIconId != 0
        if (!canHookFacetBar) {
            log(
                tagName,
                "AaUiHook: skip facet-bar override, missing resources: facet=$resLayoutGhFacetBarId, status=$resIdStatusBarId, launcherContainer=$resIdLauncherAndDashboardIconContainerId, launcherIcon=$resIdLauncherAndDashboardIconId"
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
            val resultViewGroup = param.result as ViewGroup? ?: return@hookAfter //androidx.constraintlayout.widget.ConstraintLayout
            val ctx = (param.thisObject as LayoutInflater).context
            val ctx2 = CommonContextWrapper.createAppCompatContext(ctx)
            val layoutInflater = LayoutInflater.from(ctx2)
            val resultViewGroupParent = (resultViewGroup.parent as ViewGroup?)?.apply {
                removeView(resultViewGroup)
            }
            if(closeLauncherDashboard){
                resultViewGroup.findViewById<View>(resIdLauncherAndDashboardIconId).apply {
                    setOnClickFinallyListener {
                        performLongClick()
                    }
                }
            }
            val aaFacetBar = layoutInflater.inflate(R.layout.aa_facet_bar, resultViewGroupParent, false) as ConstraintLayout
            if(autoOpen){
                aaFacetBar.post {
                    runMain {
                        delay(1000)
                        try{
                            startMethod?.invoke(null, Intent().apply {
                                component = ComponentName(BuildConfig.APPLICATION_ID, AaActivityService::class.java.name)
                                putExtra("android.intent.extra.PACKAGE_NAME", BuildConfig.APPLICATION_ID)
                            })
                        } catch (e: Throwable) {
                            log(tagName, "CarSystemUiControllerService.a start app error", e)
                        }
                    }
                }
            }
            val createBtn: (resId: Int, block: View.() -> Unit) -> Int = { resId, block ->
                val btn = ImageView(ctx).apply {
                    id = View.generateViewId()
                    layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    setImageResource(resId)
                }
                block(btn)
                aaFacetBar.addView(btn)
                btn.id
            }
            val topIds = arrayListOf(
                resIdStatusBarId,
                resIdLauncherAndDashboardIconContainerId
            )
            val bottomIds = arrayListOf(
                createBtn(R.drawable.ic_aa_home_44){
                    val intentClick = Intent().apply {
                        action = AABroadcastConst.ACTION_SCREEN_CONTROL
                        putExtra(AABroadcastConst.EXTRA_ACTION, KeyEvent.KEYCODE_HOME)
                    }
                    setOnClickListener {
                        ctx.sendBroadcast(intentClick)
                    }
                    setPadding(0, 5, 0, 5)
                },
                createBtn(R.drawable.ic_aa_fullscreen_44){
                    val intentClick = Intent().apply {
                        action = AABroadcastConst.ACTION_SCREEN_CONTROL
                        putExtra(AABroadcastConst.EXTRA_ACTION, KeyEvent.KEYCODE_APP_SWITCH)
                    }
                    setOnClickListener {
                        ctx.sendBroadcast(intentClick)
                    }
                    setPadding(0, 5, 0, 5)
                },
                createBtn(R.drawable.ic_aa_arrow_back_44){
                    val intentClick = Intent().apply {
                        action = AABroadcastConst.ACTION_SCREEN_CONTROL
                        putExtra(AABroadcastConst.EXTRA_ACTION, KeyEvent.KEYCODE_BACK)
                    }
                    setOnClickListener {
                        ctx.sendBroadcast(intentClick)
                    }
                    setPadding(0, 5, 0, 5)
                },
            )
            arrayListOf(resIdStatusBarId, resIdLauncherAndDashboardIconContainerId).forEach { vId ->
                val view = resultViewGroup.findViewById<View>(vId)
                (view.parent as ViewGroup?)?.apply {
                    removeView(view)
                }
                aaFacetBar.addView(view)
            }
            val set = ConstraintSet()
            set.clone(aaFacetBar)
            bottomIds.forEachIndexed { index, vId ->
                set.connect(vId, ConstraintSet.BOTTOM, if(index == 0) ConstraintSet.PARENT_ID else bottomIds[index-1], if(index == 0) ConstraintSet.BOTTOM else ConstraintSet.TOP, 0)
                set.connect(vId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
                set.connect(vId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
            }
            topIds.forEachIndexed { index, vId ->
                set.connect(vId, ConstraintSet.TOP, if(index == 0) ConstraintSet.PARENT_ID else topIds[index-1], if(index == 0) ConstraintSet.TOP else ConstraintSet.BOTTOM, 0)
                set.connect(vId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
                set.connect(vId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
            }
            set.applyTo(aaFacetBar)
            resultViewGroup.visibility = View.GONE
            aaFacetBar.addView(resultViewGroup)
            param.result = aaFacetBar
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
