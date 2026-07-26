package io.github.nitsuya.aa.display.xposed.hook.aa

import android.content.ContentResolver
import android.content.SharedPreferences
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.net.Uri
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.loadClass
import com.github.kyuubiran.ezxhelper.utils.putObject
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.github.nitsuya.aa.display.util.AADisplayConfig
import io.github.nitsuya.aa.display.xposed.hook.AaHook
import io.github.nitsuya.aa.display.xposed.log
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.FindMethod
import org.luckypray.dexkit.query.enums.StringMatchType
import org.luckypray.dexkit.query.matchers.MethodMatcher
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.HashMap

object AaPropsHook: AaHook() {
    override val tagName: String = "AAD_AaPropsHook"
    private const val GEARHEAD_PHENOTYPE_GROUP = "com.google.android.projection.gearhead"

    private lateinit var method: Method
    private lateinit var stringFields: List<Field>

    override fun isSupportProcess(processName: String): Boolean {
        return true
    }

    override fun loadDexClass(bridge: DexKitBridge, lpparam: XC_LoadPackage.LoadPackageParam) {
        val methodMatcher = MethodMatcher().usingStrings {
            add(
                "Must call PhenotypeContext.setContext() first",
                StringMatchType.Equals,
                false
            )
        }
        val classes = bridge.findClass {
            searchPackages = listOf("")
            matcher {
                methods {
                    add(methodMatcher)
                }
            }
        }
        if (classes.isEmpty() || classes.size > 1) {
            throw NoSuchMethodException("AaPropsHook: not found props class: ${classes.size}")
        }
        val methodDatas = classes[0].getMethods().findMethod(FindMethod().matcher(methodMatcher))
        if (methodDatas.isEmpty() || methodDatas.size > 1) {
            throw NoSuchMethodException("AaPropsHook: not found props method: ${classes.size}")
        }
        val methodData = methodDatas[0]
        val clazz = loadClass(methodData.className)
        stringFields = buildStringFieldList(clazz)
        if (stringFields.isEmpty()) {
            throw NoSuchFieldException("AaPropsHook: no non-static String fields found in ${clazz.name}")
        }
        log(tagName, "$clazz#${methodData.methodName}#stringFields=${stringFields.joinToString { it.name }}")
        method = findMethod(clazz) {
            name == methodData.methodName
        }
    }

    override fun hook(config: SharedPreferences?, lpparam: XC_LoadPackage.LoadPackageParam) {
        hookComGoogleAndroidProjectionGearheadProps(config)
        hookComGoogleAndroidGmsCarProps(config)
    }

    private fun hookComGoogleAndroidProjectionGearheadProps(config: SharedPreferences?) {
        val props = AADisplayConfig.ComGoogleAndroidProjectionGearheadProps.get(config) ?: return
        if (props.isEmpty) {
            return
        }
        val propKeys = props.keys.mapNotNull { it as? String }.toSet()
        val keyValue = HashMap<String, Any?>(props.size, 1f)
        method.hookAfter { param ->
            val thisObject = param.thisObject
            val groupAndKey = resolveGroupAndKey(thisObject, propKeys) ?: return@hookAfter
            val group = groupAndKey.first
            val key = groupAndKey.second
            if (group != GEARHEAD_PHENOTYPE_GROUP) {
                return@hookAfter
            }
            if (!props.containsKey(key)) {
                return@hookAfter
            }
            val value = keyValue.computeIfAbsent(key) {
                val value = props[key] as String
                log(tagName, "$key,$value")
                try {
                    when (param.result?.javaClass ?: return@computeIfAbsent null) {
                        String::class.java -> value
                        java.lang.Boolean::class.java, Boolean::class.java -> value.toBoolean()
                        java.lang.Long::class.java, Long::class.java -> value.toLong()
                        Integer::class.java, Int::class.java -> value.toInt()
                        else -> {
                            val result = param.result
                            value.split(",").forEach { item ->
                                val (keyName, type, valueRaw) = item.split("@", limit = 3)
                                result.putObject(
                                    keyName,
                                    when (type) {
                                        "String" -> valueRaw
                                        "Int" -> valueRaw.toInt()
                                        "Boolean" -> valueRaw.toBoolean()
                                        "Long" -> valueRaw.toLong()
                                        else -> return@forEach
                                    }
                                )
                            }
                            null
                        }
                    }
                } catch (e: Throwable) {
                    log(tagName, "Android Auto[$GEARHEAD_PHENOTYPE_GROUP] config, $key=$value convert exception", e)
                    null
                }
            }
            if (value != null) {
                param.result = value
            }
        }
    }

    private fun hookComGoogleAndroidGmsCarProps(config: SharedPreferences?) {
        val props = AADisplayConfig.ComGoogleAndroidGmsCarProps.get(config) ?: return
        if (props.isEmpty) {
            return
        }
        try {
            val matrixCursor = MatrixCursor(arrayOf("key", "value"), props.size).apply {
                props.forEach { prop ->
                    addRow(arrayOf(prop.key, prop.value))
                }
            }
            findMethod(ContentResolver::class.java) {
                name == "query" &&
                    parameterCount == 5 &&
                    parameterTypes[0] == Uri::class.java &&
                    parameterTypes[1] == Array<String>::class.java &&
                    parameterTypes[2] == String::class.java &&
                    parameterTypes[3] == Array<String>::class.java &&
                    parameterTypes[4] == String::class.java
            }.hookAfter { param ->
                val uri = param.args[0] as Uri
                if (uri.authority != "com.google.android.gms.phenotype") return@hookAfter
                if (uri.path != "/com.google.android.gms.car") return@hookAfter
                param.result = if (param.result == null) {
                    matrixCursor
                } else {
                    MergeCursor(arrayOf(param.result as Cursor, matrixCursor))
                }
            }
        } catch (e: Throwable) {
            log(tagName, "[com.google.android.gms.car] config", e)
        }
    }

    private fun buildStringFieldList(clazz: Class<*>): List<Field> {
        val result = linkedSetOf<Field>()
        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            current.declaredFields
                .filter { !java.lang.reflect.Modifier.isStatic(it.modifiers) && it.type == String::class.java }
                .forEach { field ->
                    field.isAccessible = true
                    result.add(field)
                }
            current = current.superclass
        }
        return result.toList()
    }

    private fun resolveGroupAndKey(thisObject: Any, propKeys: Set<String>): Pair<String, String>? {
        var group: String? = null
        var key: String? = null
        stringFields.forEach { field ->
            val value = runCatching { field.get(thisObject) as? String }.getOrNull() ?: return@forEach
            if (value == GEARHEAD_PHENOTYPE_GROUP) {
                group = value
                return@forEach
            }
            if (key == null && propKeys.contains(value)) {
                key = value
            }
        }
        if (group == null || key == null) return null
        return Pair(group!!, key!!)
    }
}
