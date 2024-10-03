package Shoey.RefitPlus.Kotlin

import Shoey.RefitPlus.MainPlugin.*
import Shoey.RefitPlus.refitTimerScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.*
import com.fs.state.AppDriver
import lunalib.lunaExtensions.*
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType


class Refit {

    private val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
    private val setFieldHandle = MethodHandles.lookup()
        .findVirtual(fieldClass, "set", MethodType.methodType(Void.TYPE, Any::class.java, Any::class.java))
    private val setFieldAccessibleHandle = MethodHandles.lookup()
        .findVirtual(fieldClass, "setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))

    private val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
    private val getMethodNameHandle =
        MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))
    private val invokeMethodHandle = MethodHandles.lookup().findVirtual(
        methodClass,
        "invoke",
        MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java)
    )
    private var log = Global.getLogger(this.javaClass)

    private fun hookFail()
    {
        KotlinWait = false;
        refit = null
        RefitHooked = false
        log.debug("Couldn't hook refit.")
    }

    private fun alreadyHooked()
    {
        Global.getSector().removeScriptsOfClass(refitTimerScript().javaClass)
        Global.getSector().addTransientScript(refitTimerScript())
    }

    fun getRefit(reHook: Boolean) {

        if (Global.getSettings().currentState != GameState.CAMPAIGN || cUI == null || cUI.currentCoreTab != CoreUITabId.REFIT)
            return

        log.level = logLevel

        if (reHook) {

            var state = AppDriver.getInstance().currentState
            var core = invokeMethod("getCore", state)

            var dialog = invokeMethod("getEncounterDialog", state)
            if (dialog != null) {
                core = invokeMethod("getCoreUI", dialog)
            }

            if (core is UIPanelAPI) {
                var child1 = core.getChildrenCopy().find { hasMethodOfName("setBorderInsetLeft", it) }
                if (child1 is UIPanelAPI) {
                    var child2 = child1.getChildrenCopy().find { hasMethodOfName("goBackToParentIfNeeded", it) }

                    if (child2 is UIPanelAPI) {

                        var refitParentChildrenCopy = child2.getChildrenCopy()
                        var child3 = refitParentChildrenCopy.find { hasMethodOfName("syncWithCurrentVariant", it) }

                        if (child3 !is UIPanelAPI) {
                            hookFail()
                            return
                        }

                        var refitChildren = child3.getChildrenCopy()

                        try {
                            for (ui: UIComponentAPI? in refitChildren) {
                                if (ui == null)
                                    continue
                                if (hasMethodOfName("getText", ui)) {
                                    try {
                                        var s: String? = invokeMethod("getText", ui) as String
                                        if (s != null && s == "The Game") {
                                            if (refitTimerScript.loopcount == 0) {
                                                log.debug("You lost.")
                                            }
                                            cRR.lastRefit = child3
                                            alreadyHooked()
                                            break
                                        }
                                    } catch (e: Exception)
                                    {
                                        log.error("Kotlin error caught in label")
                                    }
                                }
                            }
                        } catch (e: Exception)
                        {
                            log.error("Kotlin error caught in refitChildren")
                            hookFail()
                            return
                        }
                        log.info("Refit menu has " + refitChildren.count() + " components")
                        refit = child3
                        RefitHooked = true
                    }
                }
            }
        }
        KotlinWait = false;

    }

    fun getRefitFleetMember(): FleetMemberAPI?
    {
        try {
            return invokeMethodHandle.invoke("method", refit, null) as FleetMemberAPI
        } catch (e: Exception) {
            return null
        }
    }

    //Required to execute obfuscated methods without referencing their obfuscated class name.
    fun invokeMethod(methodName: String, instance: Any, vararg arguments: Any?): Any? {
        var method: Any? = null

        val clazz = instance.javaClass
        val args = arguments.map { it!!::class.javaPrimitiveType ?: it::class.java }
        val methodType = MethodType.methodType(Void.TYPE, args)

        method = clazz.getMethod(methodName, *methodType.parameterArray())
        try {
            return invokeMethodHandle.invoke(method, instance, arguments)
        } catch (e: Exception) {
            return null
        }
    }

    fun getChildrenCopyFromHook(panelAPI: UIPanelAPI): List<UIComponentAPI> {
        return invokeMethod("getChildrenCopy", panelAPI) as List<UIComponentAPI>
    }

    //Extends the UI API by adding the required method to get the child objects of a panel, only when used within this class.
    private fun UIPanelAPI.getChildrenCopy(): List<UIComponentAPI> {
        return invokeMethod("getChildrenCopy", this) as List<UIComponentAPI>
    }

    //Used to be able to find specific files without having to reference their obfuscated class name.
    private fun hasMethodOfName(name: String, instance: Any): Boolean {

        val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredMethods()
        return instancesOfMethods.any { getMethodNameHandle.invoke(it) == name }
    }

}