/**
 * @author nik
 */
package org.nik.presentationAssistant

import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.project.Project
import java.awt.Point
import com.intellij.openapi.Disposable
import java.awt.Color
import com.intellij.ui.JBColor
import java.awt.BorderLayout
import com.intellij.ui.components.panels.NonOpaquePanel
import java.awt.Font
import java.awt.FlowLayout
import com.intellij.ui.awt.RelativePoint
import com.intellij.openapi.wm.IdeFrame
import javax.swing.JLabel
import javax.swing.SwingConstants
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.util.Pair as IdeaPair
import java.awt.event.ActionListener
import javax.swing.SwingUtilities
import com.intellij.ui.popup.ComponentPopupBuilderImpl
import java.awt.geom.RoundRectangle2D
import javax.swing.BorderFactory
import javax.swing.KeyStroke
import java.awt.event.KeyEvent
import com.intellij.ui.IdeBorderFactory

class ActionInfoPanel(project: Project, textFragments: List<Pair<String, Font?>>) : NonOpaquePanel(BorderLayout()), Disposable {
    private val hint: JBPopup

    {
        val ideFrame = WindowManager.getInstance()!!.getIdeFrame(project)!!
        val labelsPanel = NonOpaquePanel(FlowLayout(FlowLayout.CENTER, 0, 0))
        val background = JBColor(Color(186, 238, 186, 120), Color(73, 117, 73))
        for (label in createLabels(textFragments, ideFrame)) {
            labelsPanel.add(label)
        }
        setBackground(background)
        setOpaque(true)
        add(labelsPanel, BorderLayout.CENTER)
        val arcSize = 15
        setBorder(BorderFactory.createCompoundBorder(IdeBorderFactory.createRoundedBorder(arcSize), BorderFactory.createEmptyBorder(5,10,5,10)))

        val cancelListener = IdeaPair(ActionListener {
            //this seems to be the simplest way to cancel the hint on Esc and don't consume the Esc event
            SwingUtilities.invokeLater { hint.cancel() }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0))
        hint = with (JBPopupFactory.getInstance()!!.createComponentPopupBuilder(this, this) as ComponentPopupBuilderImpl) {
            setAlpha(0.1)
            setMaskProvider { RoundRectangle2D.Double(1.0, 1.0, it!!.getWidth()-2, it.getHeight()-2, arcSize.toDouble(), arcSize.toDouble()) }
            setFocusable(false)
            setCancelKeyEnabled(false)
            setKeyboardActions(arrayListOf(cancelListener))
            createPopup()
        }

        val statusBarHeight = ideFrame.getStatusBar()!!.getComponent()!!.getHeight()
        val frame = ideFrame.getComponent()!!
        val visibleRect = frame.getVisibleRect()
        val popupSize = getPreferredSize()!!
        val point = Point(visibleRect.x + (visibleRect.width - popupSize.width)/2, visibleRect.y + visibleRect.height - popupSize.height - statusBarHeight - 5)
        hint.show(RelativePoint(frame, point))
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
        if (!hint.isDisposed()) {
            hint.cancel()
        }
    }

}
