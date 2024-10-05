package Shoey.RefitPlus.Kotlin

import Shoey.RefitPlus.MainPlugin.*
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener
import com.fs.starfarer.api.campaign.listeners.CampaignUIRenderingListener
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import org.apache.log4j.Level
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

class Dump {

    var s = ""
    var UIDump = ""

    fun returnTypesString(subject: Any) : String
    {
        var returnValue = ""

        if (subject is Boolean)
        {
            returnValue += "Boolean:"
        }
        if (subject is Boolean)
        {
            returnValue += "Boolean:"
        }

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

    fun dump(): String {
        UIDump = ""
        var test = invokeMethod("getShipDisplay", refit)
        if (test is UIPanelAPI) {
            dumpDetails(test, "", true)
        }
        UIDump = ""
        test = invokeMethod("getDesignDisplay", refit)
        if (test is UIPanelAPI) {
            dumpDetails(test, "", true)
        }
        return UIDump;
    }

    fun dumpMethods(instance: Any, prefix: String, fromWithin: Boolean) {
        val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredMethods()
        if (fromWithin)
            s+="\n"+prefix+"Class: "+instance.javaClass.toString()
        for (m: Any in instancesOfMethods)
        {
            var methodName = getMethodNameHandle.invoke(m) as String
            s += "\n"+(prefix+"Method: "+methodName)
            if (methodName.startsWith("get") || methodName.startsWith("is") && !fromWithin)
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
            var i = 0
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