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
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType


@Suppress("unused")
class RPReflect {

    private var log = Global.getLogger(this.javaClass)

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
            if (lastRefitChildrenGetShipPlace != -1 && lastRefitChildrenGetShipPlace < list.size && hasMethodOfName("getShip", list.get(lastRefitChildrenGetShipPlace)))
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
            if (coreUI != core) {
                log.debug("Newly hooked core: " + core.toString())
                coreUIChild1 = null
                coreUIChild2 = null
                refit = null
            }
            coreUI = core

        } else {
            log.error("core is not valid")
        }
    }

    var lastChild1Index = 0

    fun hookCoreChild1()
    {
        var list = try {
            coreUI.getChildrenCopy()
        } catch (e: Exception) {
            return
        }
        var hm = false
        try {
            hm = hasMethodOfName("setBorderInsetLeft", list.get(lastChild1Index))
        } catch (e: Exception) {

        }
        if (!hm) {
            timesNot++
            coreUIChild1 = list.find { hasMethodOfName("setBorderInsetLeft", it) } as UIPanelAPI
            var currentIndex = list.indexOf(coreUIChild1)
            if (currentIndex != lastChild1Index) {
                lastChild1Index = currentIndex
            }
        } else {
            coreUIChild1 = list.get(lastChild1Index) as UIPanelAPI
            timesSkipped++
        }
    }

    var lastChild2Index = 0

    fun hookCoreChild2()
    {
        var list = try {
            (coreUIChild1 as UIPanelAPI).getChildrenCopy()
        } catch (e: Exception) {
            return;
        }
        var hm = false
        try {
            hm = hasMethodOfName("goBackToParentIfNeeded", list.get(lastChild2Index))
        } catch (e: Exception) {

        }
        if (!hm) {
            timesNot++
            coreUIChild2 = list.find { hasMethodOfName("goBackToParentIfNeeded", it) } as UIPanelAPI
            var currentIndex = list.indexOf(coreUIChild2)
            if (currentIndex != lastChild2Index) {
                lastChild2Index = currentIndex
            }
        } else {
            coreUIChild2 = list.get(lastChild2Index) as UIPanelAPI
            timesSkipped++
        }
    }

    fun hookRefit() {

        if (Global.getSettings().currentState != GameState.CAMPAIGN || cUI == null || cUI.currentCoreTab != CoreUITabId.REFIT)
            return

        log.level = logLevel

        if (!coreUI.getChildrenCopy().contains(coreUIChild1)) {
            log.debug("Rehooking child 1")
            hookCoreChild1()
            return
        }

        if (!coreUIChild1.getChildrenCopy().contains(coreUIChild2)) {
            log.debug("Rehooking child 2")
            hookCoreChild2()
            return
        }

        var refitParentChildrenCopy = coreUIChild2.getChildrenCopy()
        var child3 = refitParentChildrenCopy.find { hasMethodOfName("syncWithCurrentVariant", it) }

        if (child3 !is UIPanelAPI) {
            return
        }


        if (refit == null || child3.getChildrenCopy() != refit.getChildrenCopy())
        {
            refit = child3
            insertOverlay(refit)
        }

    }

    //Extends the UI API by adding the required method to get the child objects of a panel, only when used within this class.
    private fun UIPanelAPI.getChildrenCopy(): List<UIComponentAPI> {
        return getChildrenCopyFromHook(this)
    }

    //Used to be able to find specific files without having to reference their obfuscated class name.
    private fun hasMethodOfName(name: String, instance: Any): Boolean {
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