@file:Suppress("LiftReturnOrAssignment")

package Shoey.RefitPlus.Kotlin

import Shoey.RefitPlus.MainPlugin
import Shoey.RefitPlus.MainPlugin.*
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.*
import com.fs.state.AppDriver
import lunalib.lunaExtensions.*
import org.apache.log4j.Level
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType


@Suppress("unused")
class RPReflect {

    private var log = Global.getLogger(this.javaClass)
    var core: Any? = null
    var child1: Any? = null
    var child2: Any? = null

    var s = ""

    fun getRefitFleetMember(): FleetMemberAPI?
    {
        try {
            return invokeMethod("getMember", refit) as FleetMemberAPI
        } catch (e: Exception) {
            return null
        }
    }

    var lastRefitChildrenGetShipPlace = 0

    fun getRefitShipAPI(): ShipAPI?
    {
        var ship: Any? = null
        try {
            var list = refit.getChildrenCopy()
            if (lastRefitChildrenGetShipPlace < list.size && hasMethodOfName("getShip", list.get(lastRefitChildrenGetShipPlace)))
            {
                ship = invokeMethod("getShip", list.get(lastRefitChildrenGetShipPlace))
            }
            if (ship !is ShipAPI) {
                for (c: Any? in list) {
                    if (c == null || !hasMethodOfName("getShip", c)) {
                        continue
                    }

                    ship = invokeMethod("getShip", c)
                    if (ship is ShipAPI) {
                        timesNot++
                        lastRefitChildrenGetShipPlace = list.indexOf(c)
                        break
                    }
                }
            } else {
                timesSkipped++
            }
        } catch (e: Exception) {
            log.debug("ShipAPI failsafe")
            return null
        }

        if (ship is ShipAPI) {
            if (MainPlugin.ship != ship) {
                MainPlugin.ship = ship
                log.debug("Found " + ship)
            }
            return ship
        }

        return null
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
                reflectionCount++
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

    private fun hookFail()
    {
        unhook()
        log.debug("Couldn't hook refit.")
    }

    private fun alreadyHooked(hookedRefit: UIPanelAPI)
    {

    }

    fun unhook()
    {
        core = null
        child1 = null
        child2 = null
    }

    fun hookCore()
    {
        log.level = logLevel

        var state = AppDriver.getInstance().currentState
        var core2 = invokeMethod("getCore", state)

        var dialog = invokeMethod("getEncounterDialog", state)
        if (dialog != null) {
            core2 = invokeMethod("getCoreUI", dialog)
        }

        if (core2 is UIPanelAPI) {
            if (coreUI != core2) {
                log.info("Newly hooked core: " + core2.toString())
                child1 = null
                child2 = null
                refit = null
            }
            coreUI = core2
            core = core2

        } else {
            log.error("core is not valid")
        }
    }

    var lastChild1Index = 0

    fun hookCoreChild1()
    {
        var currentList = coreUI.getChildrenCopy()
        if (!hasMethodOfName("setBorderInsetLeft", currentList.get(lastChild1Index))) {
            timesNot
            child1 = currentList.find { hasMethodOfName("setBorderInsetLeft", it) }
            var currentIndex = currentList.indexOf(child1)
            if (currentIndex != lastChild1Index) {
                lastChild1Index = currentIndex
            }
        } else {
            timesSkipped++
        }
    }

    fun hookCoreChild2()
    {
        var currentList = (child1 as UIPanelAPI).getChildrenCopy()
        child2 = currentList.find { hasMethodOfName("goBackToParentIfNeeded", it) }
    }

    fun hookRefit() {
        hookRefit(false)
    }

    fun hookRefit(reHook: Boolean) {

        if (Global.getSettings().currentState != GameState.CAMPAIGN || cUI == null || cUI.currentCoreTab != CoreUITabId.REFIT)
            return

        log.level = logLevel

        if (core !is UIPanelAPI || reHook) {
            hookCore()
        }

        if (core !is UIPanelAPI)
        {
            unhook()
            log.error("Couldn't hook core.")
            return
        }

        if (child1 !is UIPanelAPI || reHook)
        {
            hookCoreChild1()
        }

        if (child1 !is UIPanelAPI)
        {
            unhook()
            log.error("Couldn't hook child1.")
            return
        }

        if (child2 !is UIPanelAPI || reHook)
        {
            hookCoreChild2()
        }

        if (child2 !is UIPanelAPI)
        {
            unhook()
            log.error("Couldn't hook child2.")
            return
        }

        var refitParentChildrenCopy = (child2 as UIPanelAPI).getChildrenCopy()
        var child3 = refitParentChildrenCopy.find { hasMethodOfName("syncWithCurrentVariant", it) }

        if (child3 !is UIPanelAPI) {
            hookFail()
            return
        }


        if (refit == null || child3.getChildrenCopy() != refit.getChildrenCopy())
        {
            refit = child3
            insertOverlay()
        }

    }

    //Extends the UI API by adding the required method to get the child objects of a panel, only when used within this class.
    private fun UIPanelAPI.getChildrenCopy(): List<UIComponentAPI> {
        return getChildrenCopyFromHook(this)
    }

    //Used to be able to find specific files without having to reference their obfuscated class name.
    private fun hasMethodOfName(name: String, instance: Any): Boolean {
        if (logLevel == Level.DEBUG) {
            reflectionCount++
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