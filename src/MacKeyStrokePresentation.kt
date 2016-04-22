/**
 * @author nik
 */
package org.nik.presentationAssistant

import com.intellij.openapi.keymap.MacKeymapUtil
import java.awt.Font
import java.awt.GraphicsEnvironment

/*
val macKeyStokesFont by lazy {
    computeMacKeyStokesFont()
}
*/

private fun computeMacKeyStokesFont() =
        GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts
        .minBy { getNonDisplayableMacSymbols(it).size }

private var macKeyStokesFont: Font? = null
private var macKeyStokesFontComputed: Boolean = false
fun getMacKeyStokesFont(): Font? {
    if (!macKeyStokesFontComputed) {
        macKeyStokesFont = computeMacKeyStokesFont()
        macKeyStokesFontComputed = true
    }
    return macKeyStokesFont
}

private fun getNonDisplayableMacSymbols(font: Font) =
        MacKeymapUtil::class.java.declaredFields
                .filter { it.type == String::class.java && it.name != "APPLE"
                          && font.canDisplayUpTo(it.get(null) as String) != -1 }
                .map { it.name }
