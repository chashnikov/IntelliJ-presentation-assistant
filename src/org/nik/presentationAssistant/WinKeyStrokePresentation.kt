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

import java.awt.event.KeyEvent

//repeats logic from com.intellij.openapi.keymap.KeymapUtil.getKeyText but don't use Mac presentation on Mac
fun getWinKeyText(code: Int): String {
    when (code) {
        KeyEvent.VK_BACK_QUOTE -> return "`"
        KeyEvent.VK_SEPARATOR -> return ","
        KeyEvent.VK_DECIMAL -> return "."
        KeyEvent.VK_SLASH -> return "/"
        KeyEvent.VK_BACK_SLASH -> return "\\"
        KeyEvent.VK_PERIOD -> return "."
        KeyEvent.VK_SEMICOLON -> return ";"
        KeyEvent.VK_CLOSE_BRACKET -> return "]"
        KeyEvent.VK_OPEN_BRACKET -> return "["
        KeyEvent.VK_EQUALS -> return "="
    }
    return KeyEvent.getKeyText(code)
}
