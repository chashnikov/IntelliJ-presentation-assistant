/**
 * @author nik
 */
package org.nik.presentationAssistant

import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.project.Project
import java.awt.Point
import com.intellij.openapi.Disposable
import java.awt.Color
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.ui.JBColor
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.BorderFactory
import java.awt.BorderLayout
import com.intellij.openapi.wm.IdeFrame
import com.intellij.ui.components.panels.NonOpaquePanel

class ActionInfoPanel(project: Project, text: String) : NonOpaquePanel(BorderLayout()), Disposable {
    private val balloon: Balloon

    {
        val ideFrame = WindowManager.getInstance()!!.getIdeFrame(project)!!
        add(createLabel(text, ideFrame), BorderLayout.CENTER)
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
        val background = JBColor(Color(186, 238, 186, 120), Color(73, 117, 73))

        balloon = with (JBPopupFactory.getInstance()!!.createBalloonBuilder(this)) {
            setFillColor(background)
            setShowCallout(false)
            setAnimationCycle(0)
            createBalloon()
        }

        val statusBarHeight = ideFrame.getStatusBar()!!.getComponent()!!.getHeight()
        val frame = ideFrame.getComponent()!!
        val visibleRect = frame.getVisibleRect()
        var popupHeight = getPreferredSize()!!.height
        val point = Point(visibleRect.x + (visibleRect.width)/2, visibleRect.y + visibleRect.height - popupHeight - statusBarHeight - 5)
        balloon.show(RelativePoint(frame, point), Balloon.Position.below)
    }

    private fun createLabel(text: String, ideFrame: IdeFrame): JLabel {
        val fontSize = getPresentationAssistant().configuration.fontSize
        val label = JLabel("<html>$text</html>", SwingConstants.CENTER)
        label.setFont(label.getFont()!!.deriveFont(fontSize.toFloat()))
        val frameWidth = ideFrame.getComponent()!!.getWidth()
        if (frameWidth > 100) {
            while (label.getPreferredSize()!!.width > frameWidth - 10 && label.getFont()!!.getSize() > 12) {
                label.setFont(label.getFont()!!.deriveFont(label.getFont()!!.getSize().toFloat()-1))
            }
        }
        return label
    }

    public override fun dispose() {
        if (!balloon.isDisposed()) {
            balloon.hide()
        }
    }

}
