/**
 * @author nik
 */
package org.nik.presentationAssistant

import java.awt.Font
import com.intellij.openapi.keymap.MacKeymapUtil
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.diagnostic.Logger
import kotlin.properties.Delegates
import java.awt.GraphicsEnvironment

val LOG = Logger.getInstance("#org.nik.presentationAssistant.MacKeyStrokePresentation")

val macKeyStokesFont by lazy {
    GraphicsEnvironment.getLocalGraphicsEnvironment()!!.getAllFonts()!!
            .minBy { getNonDisplayableMacSymbols(it).size }
}

fun getNonDisplayableMacSymbols(font: Font) =
        MacKeymapUtil::class.java.getDeclaredFields()
                .filter { it.getType() == String::class.java && it.getName() != "APPLE"
                          && font.canDisplayUpTo(it.get(null) as String) != -1 }
                .map { it.getName()!! }
