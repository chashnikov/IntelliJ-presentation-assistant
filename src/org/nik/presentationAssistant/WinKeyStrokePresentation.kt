/*
 * Copyright 2000-2016 Nikolay Chashnikov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author nik
 */
package org.nik.presentationAssistant

import com.intellij.openapi.util.text.StringUtil
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.lang.reflect.Modifier

val inputEventMaskFieldNames by lazy {
    InputEvent::class.java.fields
    .filter {
        it.name.endsWith("_MASK") && !it.name.endsWith("_DOWN_MASK")
                && !it.name.startsWith("BUTTON")
                && Modifier.isStatic(it.modifiers) && it.get(null) is Int
    }
    .map { Pair(fieldNameToPresentableName(it.name.removeSuffix("_MASK")), it.get(null) as Int) }
}

fun getWinModifiersText(modifiers: Int) =
        inputEventMaskFieldNames
                .filter { modifiers and (it.second) != 0}
                .map { it.first }
                .joinToString("+")

val keyEventFieldNames by lazy {
    KeyEvent::class.java.fields
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
        in keyEventFieldNames.keys -> keyEventFieldNames[key]
        else -> "Unknown key 0x${Integer.toHexString(key)}"
    }

fun fieldNameToPresentableName(name: String) = name.split('_').map { StringUtil.capitalize(it.toLowerCase()) }.joinToString(" ")
