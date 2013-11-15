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
import javax.swing.BorderFactory
import java.awt.BorderLayout
import com.intellij.openapi.wm.IdeFrame
import com.intellij.ui.components.panels.NonOpaquePanel
import java.awt.Font
import javax.swing.SwingConstants
import java.awt.FlowLayout

class ActionInfoPanel(project: Project, textFragments: List<Pair<String, Font?>>) : NonOpaquePanel(BorderLayout()), Disposable {
    private val balloon: Balloon

    {
        val ideFrame = WindowManager.getInstance()!!.getIdeFrame(project)!!
        val labelsPanel = NonOpaquePanel(FlowLayout(FlowLayout.CENTER, 0, 0))
        for (label in createLabels(textFragments, ideFrame)) {
            labelsPanel.add(label)
        }
        add(labelsPanel, BorderLayout.CENTER)
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

    private fun createLabels(textFragments: List<Pair<String, Font?>>, ideFrame: IdeFrame): List<JLabel> {
        var fontSize = getPresentationAssistant().configuration.fontSize.toFloat()
        val labels = textFragments.map {
            val label = JLabel("<html>${it.first}</html>", SwingConstants.CENTER)
            if (it.second != null) label.setFont(it.second)
            label
        }
        fun setFontSize(size: Float) {
            for (label in labels) {
                label.setFont(label.getFont()!!.deriveFont(size))
            }
        }
        setFontSize(fontSize)
        val frameWidth = ideFrame.getComponent()!!.getWidth()
        if (frameWidth > 100) {
            while (labels.map {it.getPreferredSize()!!.width}.sum() > frameWidth - 10 && fontSize > 12) {
                setFontSize(--fontSize)
            }
        }
        return labels
    }

    public override fun dispose() {
        if (!balloon.isDisposed()) {
            balloon.hide()
        }
    }

}
