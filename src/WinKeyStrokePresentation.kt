/**
 * @author nik
 */
package org.nik.presentationAssistant

import com.intellij.openapi.util.text.StringUtil
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.lang.reflect.Modifier

//val inputEventMaskFieldNames by lazy { computeEventMaskFieldNames() }
private var inputEventMaskFieldNames: List<Pair<String, Int>>? = null

private fun getInputEventMaskFieldNames(): List<Pair<String, Int>> {
    if (inputEventMaskFieldNames == null) {
        inputEventMaskFieldNames = computeEventMaskFieldNames()
    }
    return inputEventMaskFieldNames!!
}

private fun computeEventMaskFieldNames() =
        InputEvent::class.java.fields
        .filter {
            it.name.endsWith("_MASK") && !it.name.endsWith("_DOWN_MASK")
                    && !it.name.startsWith("BUTTON")
                    && Modifier.isStatic(it.modifiers) && it.get(null) is Int
        }
        .map { Pair(fieldNameToPresentableName(it.name.removeSuffix("_MASK")), it.get(null) as Int) }

fun getWinModifiersText(modifiers: Int) =
        getInputEventMaskFieldNames()
                .filter { modifiers and (it.second) != 0}
                .map { it.first }
                .joinToString("+")

/*
val keyEventFieldNames by lazy {
    computeKeyEventFieldNames()
}
*/

private var keyEventFieldNamesCached: Map<Int, String>? = null
private fun getKeyEventFieldNames(): Map<Int, String> {
    if (keyEventFieldNamesCached == null) {
        keyEventFieldNamesCached = computeKeyEventFieldNames()
    }
    return keyEventFieldNamesCached!!
}

private fun computeKeyEventFieldNames(): Map<Int, String> {
    return KeyEvent::class.java.fields
            .filter { it.name.startsWith("VK_") && Modifier.isStatic(it.modifiers) && it.get(null) is Int }
            .map { Pair(fieldNameToPresentableName(it.name.removePrefix("VK_")), it.get(null) as Int) }
            .groupBy { it.second }
            .mapValues { it.value.first().first }
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
        in getKeyEventFieldNames().keys -> getKeyEventFieldNames()[key]
        else -> "Unknown key 0x${Integer.toHexString(key)}"
    }
        

fun fieldNameToPresentableName(name: String) = name.split('_').map { StringUtil.capitalize(it.toLowerCase()) }.joinToString(" ")
