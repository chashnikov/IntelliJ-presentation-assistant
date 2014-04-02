/**
 * @author nik
 */
package org.nik.presentationAssistant

import java.awt.event.InputEvent
import com.intellij.openapi.util.text.StringUtil
import kotlin.properties.Delegates
import java.awt.event.KeyEvent
import java.lang.reflect.Modifier

val inputEventMaskFieldNames by Delegates.lazy {
    javaClass<InputEvent>().getFields()
            .filter { it.getName()!!.endsWith("_MASK") && !it.getName()!!.endsWith("_DOWN_MASK")
                      && !it.getName()!!.startsWith("BUTTON")
                      && Modifier.isStatic(it.getModifiers()) && it.get(null) is Int}
            .map { Pair(fieldNameToPresentableName(it.getName()!!.trimTrailing("_MASK")), it.get(null) as Int)}
}

fun getWinModifiersText(modifiers: Int) =
        inputEventMaskFieldNames
                .filter { modifiers and (it.second) != 0}
                .map { it.first }
                .makeString("+")

val keyEventFieldNames by Delegates.lazy {
    javaClass<KeyEvent>().getFields()
            .filter { it.getName()!!.startsWith("VK_") && Modifier.isStatic(it.getModifiers()) && it.get(null) is Int}
            .map { Pair(fieldNameToPresentableName(it.getName()!!.trimLeading("VK_")), it.get(null) as Int)}
            .groupBy { it.second }
            .mapValues { it.value.first!!.first }
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
        in keyEventFieldNames.keySet() -> keyEventFieldNames[key]
        else -> "Unknown key 0x${Integer.toHexString(key)}"
    }
        

fun fieldNameToPresentableName(name: String) = name.split('_').map { StringUtil.capitalize(it.toLowerCase()) }.makeString(" ")
