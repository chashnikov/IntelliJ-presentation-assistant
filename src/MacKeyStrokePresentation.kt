/**
 * @author nik
 */
package org.nik.presentationAssistant

import com.intellij.openapi.keymap.MacKeymapUtil
import java.awt.Font
import java.awt.GraphicsEnvironment

val macKeyStokesFont by lazy {
    GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts
            .minBy { getNonDisplayableMacSymbols(it).size }
}

private fun getNonDisplayableMacSymbols(font: Font) =
        MacKeymapUtil::class.java.declaredFields
                .filter { it.type == String::class.java && it.name != "APPLE"
                          && font.canDisplayUpTo(it.get(null) as String) != -1 }
                .map { it.name }
