/**
 * @author nik
 */
package org.nik.presentationAssistant

import java.awt.event.InputEvent
import com.intellij.openapi.util.text.StringUtil
import kotlin.properties.Delegates
import java.awt.event.KeyEvent
import java.lang.reflect.Modifier

val inputEventMaskFieldNames by lazy {
    InputEvent::class.java.getFields()
            .filter { it.getName()!!.endsWith("_MASK") && !it.getName()!!.endsWith("_DOWN_MASK")
                      && !it.getName()!!.startsWith("BUTTON")
                      && Modifier.isStatic(it.getModifiers()) && it.get(null) is Int}
            .map { Pair(fieldNameToPresentableName(it.getName()!!.removeSuffix("_MASK")), it.get(null) as Int)}
}

fun getWinModifiersText(modifiers: Int) =
        inputEventMaskFieldNames
                .filter { modifiers and (it.second) != 0}
                .map { it.first }
                .joinToString("+")

val keyEventFieldNames by lazy {
    KeyEvent::class.java.getFields()
            .filter { it.getName()!!.startsWith("VK_") && Modifier.isStatic(it.getModifiers()) && it.get(null) is Int}
            .map { Pair(fieldNameToPresentableName(it.getName()!!.removePrefix("VK_")), it.get(null) as Int)}
            .groupBy { it.second }
            .mapValues { it.value.first()!!.first }
}

fun getWinKeyText(key: Int) =
    when (key) {
        KeyEvent.VK_BACK_SPACE -> "Backspace"
        KeyEvent.VK_MULTIPLY -> "NumPad *"
        KeyEvent.VK_ADD -> "NumPad +"
        KeyEvent.VK_SEPARATOR -> "NumPad ,"
        KeyEvent.VK_SUBTRACT -> "NumPad -"
        KeyEvent.VK_DECIMAL -> "NumPad ."
        KeyEvent.VK_DIVIDE -> "NumPad /"
        in keyEventFieldNames.keys -> keyEventFieldNames[key]
        else -> "Unknown key 0x${Integer.toHexString(key)}"
    }
        

fun fieldNameToPresentableName(name: String) = name.split('_').map { StringUtil.capitalize(it.toLowerCase()) }.joinToString(" ")
