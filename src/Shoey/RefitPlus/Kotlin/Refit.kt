@file:Suppress("LiftReturnOrAssignment")

package Shoey.RefitPlus.Kotlin

import Shoey.RefitPlus.MainPlugin.*
import Shoey.RefitPlus.refitTimerScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener
import com.fs.starfarer.api.campaign.listeners.CampaignUIRenderingListener
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.*
import com.fs.state.AppDriver
import lunalib.lunaExtensions.*
import org.apache.log4j.Level
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType


@Suppress("unused")
class Refit {

    private var log = Global.getLogger(this.javaClass)
    var s = ""
    private fun hookFail()
    {
        KotlinWait = false
        cRL.refit = null
        RefitHooked = false
        log.debug("Couldn't hook refit.")
    }

    private fun alreadyHooked(hookedRefit: UIPanelAPI)
    {
        cRL.lastRefit = hookedRefit
        Global.getSector().removeScriptsOfClass(refitTimerScript().javaClass)
        Global.getSector().addTransientScript(refitTimerScript())
    }

    fun hookCore()
    {
        log.level = logLevel

        var state = AppDriver.getInstance().currentState
        var core = invokeMethod("getCore", state)

        var dialog = invokeMethod("getEncounterDialog", state)
        if (dialog != null) {
            core = invokeMethod("getCoreUI", dialog)
        }

        if (core is UIPanelAPI) {
            cRL.coreUI = core
        } else {
            log.error("core is not valid")
        }
    }

    fun getRefitFleetMember(): FleetMemberAPI?
    {
        if (logLevel == Level.DEBUG) {
            reflectionCount++;
        }
        try {
            return invokeMethod("getMember", cRL.refit) as FleetMemberAPI
        } catch (e: Exception) {
            return null
        }
    }

    fun getRefitShipAPI(): ShipAPI?
    {
        if (logLevel == Level.DEBUG) {
            reflectionCount++;
        }
        try {
            for (c: Any? in cRL.refit.getChildrenCopy())
            {
                if (c == null || !hasMethodOfName("getShip", c))
                {
                    continue;
                }
                var ship = invokeMethod("getShip", c)
                if (ship is ShipAPI) {
                    if (cRL.ship != ship)
                        log.debug("Found "+ship)
                    return ship
                }
            }
        } catch (e: Exception) {
            log.debug("ShipAPI failsafe")
            return cRL.ship
        }
        return cRL.ship
    }

    fun returnTypesString(subject: Any) : String
    {
        var returnValue = ""

        if (subject is UIPanelAPI)
        {
            returnValue += "UIPanelAPI:"
        }
        if (subject is UIComponentAPI)
        {
            returnValue += "UIComponentAPI:"
        }
        if (subject is ShipAPI)
        {
            returnValue += "ShipAPI:"
        }
        if (subject is FleetMemberAPI)
        {
            returnValue += "FleetMemberAPI:"
        }
        if (subject is MutableShipStatsAPI)
        {
            returnValue += "MutableShipStatsAPI:"
        }
        if (subject is CampaignInputListener)
        {
            returnValue += "CampaignInputListener:"
        }
        if (subject is CampaignUIRenderingListener)
        {
            returnValue += "CampaignUIRenderingListener:"
        }
        if (returnValue == "") {
            returnValue = "Unknown"
        }
        return returnValue
    }

    //Required to execute obfuscated methods without referencing their obfuscated class name.
    fun invokeMethod(methodName: String, instance: Any, vararg arguments: Any?): Any? {
        lateinit var method: Any

        val clazz = instance.javaClass
        val args = arguments.map { it!!::class.javaPrimitiveType ?: it::class.java }
        val methodType = MethodType.methodType(Void.TYPE, args)

        method = clazz.getMethod(methodName, *methodType.parameterArray())
        try {
            if (logLevel == Level.DEBUG) {
                reflectionCount++;
            }
            return invokeMethodHandle.invoke(method, instance, arguments)
        } catch (e: Exception) {
            return null
        }
    }

    fun getChildrenCopyFromHook(panelAPI: UIPanelAPI): List<UIComponentAPI> {
        return invokeMethod("getChildrenCopy", panelAPI) as List<UIComponentAPI>
    }

    fun getChildrenCopyFromHook(instance: Any): List<Any?> {
        return invokeMethod("getChildrenCopy", instance) as List<Any?>
    }

    fun dumpMethods(instance: Any, prefix: String, fromWithin: Boolean) {
        val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredMethods()
        if (fromWithin)
            s+="\n"+prefix+"Class: "+instance.javaClass.toString()
        for (m: Any in instancesOfMethods)
        {
            var methodName = getMethodNameHandle.invoke(m) as String
            s += "\n"+(prefix+"Method: "+methodName)
            if (methodName.contains("get") && !fromWithin)
            {
                try {
                    var invoked = invokeMethod(methodName, instance) as Any
                    s += "\nReturns " + prefix + returnTypesString(invoked) + invoked.toString()
                    if (methodName.contains("Listener")) {
                        s += "\n"+prefix+"{"
                        dumpMethods(invoked, "    " + prefix, true)
                        s += "\n"+prefix+"}"

                    }

                } catch (e: Exception)
                {

                }
            }
        }

    }

    fun dumpMethods(instance: Any, prefix: String) {
        dumpMethods(instance, prefix, false)
    }


    fun dumpDetails(list: List<Any?>, prefix: String)
    {
        try {
            for (ui: Any? in list) {
                if (ui == null)
                    continue
//                for (s: String in ReflectionUtils.getFieldsOfType(ui, String::class.java))
//                {
//                    UIDump += "\n"+("String: "+ui.toString()+": "+s+", "+(ReflectionUtils.get(s, ui) as String))
//                }

                dumpDetails(ui,prefix)
            }
        } catch (e: Exception)
        {
            UIDump += "\n"+("Couldn't dump.")
        }
    }

    fun dumpText(instance: Any, prefix: String)
    {
        try {
            if (hasMethodOfName("getText", instance)) {
                s += "\n"+(prefix+"Text: " + (invokeMethod("getText", instance) as String?))
            }
        } catch (e: Exception)
        {
        }

    }


    fun dumpDetails(instance: Any, prefix: String, recursive: Boolean) {
        s = ""
        try {
            s += "\n" + (prefix + "Instance: " + instance)
            s += "\n" + (prefix + "Class: " + instance.javaClass)
            s += "\n" + (prefix + "Types: " + returnTypesString(instance))
            dumpText(instance, prefix)
            s += "\n" + (prefix) + "{"
            dumpMethods(instance, "    " + prefix)
            s += "\n" + (prefix) + "}"
        } catch (e: Exception) {
            TODO("Not yet implemented")
        }
        UIDump += s
        if (recursive) {
            var i = 0;
            try {
                i = getChildrenCopyFromHook(instance).size
            } catch (e: Exception) {
            }
            if (i != 0) {
                UIDump += "\n" + (prefix) + "{"
                dumpDetails(getChildrenCopyFromHook(instance), "    " + prefix)
                UIDump += "\n" + (prefix) + "}"
            }
        }
    }

    fun dumpDetails(instance: Any, prefix: String)
    {
        dumpDetails(instance, prefix, true)
    }

    fun getRefit(reHook: Boolean) {

        if (Global.getSettings().currentState != GameState.CAMPAIGN || cUI == null || cUI.currentCoreTab != CoreUITabId.REFIT)
            return

        log.level = logLevel

        if (reHook) {

            if (cRL.coreUI is UIPanelAPI) {
                var currentList = cRL.coreUI.getChildrenCopy()
                var child1 = currentList.find { hasMethodOfName("setBorderInsetLeft", it) }
                if (child1 is UIPanelAPI) {
                    currentList = child1.getChildrenCopy()
                    var child2 = currentList.find { hasMethodOfName("goBackToParentIfNeeded", it) }

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
                                if (ui is LabelAPI)
                                {
                                    if (ui.text == cRL.labels.get(0).text)
                                    {
                                        log.debug(ui.toString()+" is "+cRL.labels.get(0))
                                        alreadyHooked(child3);
                                        return;
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
                        cRL.refit = child3
                        if (UIDump == "dump") {
                            UIDump = "";
                            var test = invokeMethod("getShipDisplay", cRL.refit);
                            if (test is UIPanelAPI) {
                                dumpDetails(test, "", true)
                            }
                            log.debug(UIDump);
                            UIDump = "";
                            test = invokeMethod("getDesignDisplay", cRL.refit);
                            if (test is UIPanelAPI) {
                                dumpDetails(test, "", true)
                            }
                            log.debug(UIDump);
                            UIDump = "dump";
                        }
                        cRL.InsertOverlay()

                        RefitHooked = true
                    }
                }
            } else
                log.error("Refit hook not successful because coreUI is not valid.")
        }
        KotlinWait = false

    }

    //Extends the UI API by adding the required method to get the child objects of a panel, only when used within this class.
    private fun UIPanelAPI.getChildrenCopy(): List<UIComponentAPI> {
        return getChildrenCopyFromHook(this)
    }

    //Used to be able to find specific files without having to reference their obfuscated class name.
    private fun hasMethodOfName(name: String, instance: Any): Boolean {
        if (logLevel == Level.DEBUG) {
            reflectionCount++;
        }
        val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredMethods()
        return instancesOfMethods.any { getMethodNameHandle.invoke(it) == name }
    }

    private val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
    private val setFieldHandle = MethodHandles.lookup()
        .findVirtual(fieldClass, "set", MethodType.methodType(Void.TYPE, Any::class.java, Any::class.java))
    private val setFieldAccessibleHandle = MethodHandles.lookup()
        .findVirtual(fieldClass, "setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))

    val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
    val getMethodNameHandle =
        MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))
    val invokeMethodHandle = MethodHandles.lookup().findVirtual(
        methodClass,
        "invoke",
        MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java)
    )

}