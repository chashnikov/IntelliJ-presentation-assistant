/**
 * @author nik
 */
package org.nik.presentationAssistant

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.actionSystem.Shortcut
import com.intellij.openapi.actionSystem.ActionGroup
import java.util.HashMap
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import javax.swing.KeyStroke
import com.intellij.openapi.keymap.MacKeymapUtil
import org.nik.presentationAssistant.ShortcutPresenter.KeymapKind
import java.awt.Font
import java.util.ArrayList
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.application.ApplicationManager

public class ShortcutPresenter() : Disposable {
    private val movingActions = setOf(
            "EditorLeft", "EditorRight", "EditorDown", "EditorUp",
            "EditorLineStart", "EditorLineEnd", "EditorPageUp", "EditorPageDown",
            "EditorPreviousWord", "EditorNextWord",
            "EditorScrollUp", "EditorScrollDown",
            "EditorTextStart", "EditorTextEnd",
            "EditorDownWithSelection", "EditorUpWithSelection",
            "EditorRightWithSelection", "EditorLeftWithSelection",
            "EditorLineStartWithSelection", "EditorLineEndWithSelection",
            "EditorPageDownWithSelection", "EditorPageUpWithSelection")

    private val typingActions = setOf(IdeActions.ACTION_EDITOR_BACKSPACE, IdeActions.ACTION_EDITOR_ENTER,
            IdeActions.ACTION_EDITOR_NEXT_TEMPLATE_VARIABLE)
    private val parentGroupIds = setOf("CodeCompletionGroup", "FoldingGroup", "GoToMenu", "IntroduceActionsGroup")
    private val winKeymap = KeymapManager.getInstance()!!.getKeymap("\$default")
    private val macKeymap = KeymapManager.getInstance()!!.getKeymap("Mac OS X");
    private var infoPanel: ActionInfoPanel? = null
    private val parentNames = HashMap<String, String>();

    {
        enable()
    }

    private fun isPopupGroup(a: AnAction) = (a as? ActionGroup)?.isPopup() ?: false

    private fun fillParentNames(group: ActionGroup, parentName: String) {
        val actionManager = ActionManager.getInstance()!!
        for (item in group.getChildren(null)) {
            when (item) {
                is ActionGroup -> {
                    if (!item.isPopup()) fillParentNames(item, parentName)
                }
                else -> {
                    val id = actionManager.getId(item)
                    if (id != null) {
                        parentNames[id] = parentName
                    }
                }
            }
        }

    }

    private fun enable() {
        val actionManager = ActionManager.getInstance()!!
        for (groupId in parentGroupIds) {
            val group = actionManager.getAction(groupId)
            if (group is ActionGroup) {
                fillParentNames(group, group.getTemplatePresentation().getText()!!)
            }
        }

        actionManager.addAnActionListener(object: AnActionListener {
            var currentAction: ActionData? = null

            public override fun beforeActionPerformed(action: AnAction?, dataContext: DataContext?, event: AnActionEvent?) {
                currentAction = null
                val actionId = ActionManager.getInstance()?.getId(action!!)
                if (actionId == null) return

                if (!movingActions.contains(actionId) && !typingActions.contains(actionId)) {
                    val project = event!!.getProject()
                    val text = event.getPresentation().getText()
                    currentAction = ActionData(actionId, project, text)
                }
            }

            public override fun afterActionPerformed(action: AnAction?, dataContext: DataContext?, event: AnActionEvent?) {
                val actionData = currentAction
                val actionId = ActionManager.getInstance()?.getId(action!!)
                if (actionData != null && actionData.actionId == actionId) {
                    ApplicationManager.getApplication()!!.invokeLater { showActionInfo(actionData) }
                    showActionInfo(actionData)
                }
            }

            public override fun beforeEditorTyping(c: Char, dataContext: DataContext?) { }
        }, this)
    }

    enum class KeymapKind { WIN; MAC }

    fun getCurrentOSKind() = when {
        SystemInfo.isMac -> KeymapKind.MAC
        else -> KeymapKind.WIN
    }

    fun KeymapKind.getAlternativeKind() = when (this) {
        KeymapKind.WIN -> KeymapKind.MAC
        KeymapKind.MAC -> KeymapKind.WIN
    }

    fun KeymapKind.getKeymap() = when (this) {
        KeymapKind.WIN -> winKeymap
        KeymapKind.MAC -> macKeymap
    }

    fun KeymapKind.getDisplayName() = when (this) {
        KeymapKind.WIN -> "Win/Linux"
        KeymapKind.MAC -> "Mac"
    }

    class ActionData(val actionId: String, val project: Project?, val actionText: String?)

    public fun showActionInfo(actionData: ActionData) {
        val actionId = actionData.actionId
        val currentShortcut = shortcutText(getCurrentOSKind().getKeymap()?.getShortcuts(actionId), getCurrentOSKind())
        val alternativeKind = getCurrentOSKind().getAlternativeKind()
        val alternativeShortcut = shortcutText(alternativeKind.getKeymap()?.getShortcuts(actionId), alternativeKind)
        val parentGroupName = parentNames[actionId]
        val actionText = (if (parentGroupName != null) "$parentGroupName ${MacKeymapUtil.RIGHT} " else "") + (actionData.actionText ?: "").trimTrailing("...")
        val fragments = ArrayList<Pair<String, Font?>>()
        val content = StringBuilder()
        if (actionText.length > 0) {
            content.append("<b>${actionText}</b>")
        }
        if (currentShortcut.length > 0 && currentShortcut != actionText) {
            if (content.length > 0) content.append(" via ")
            content.append(currentShortcut)
        }
        if (alternativeShortcut.length > 0 && alternativeShortcut != currentShortcut) {
            val altText = "for ${alternativeKind.getDisplayName()}"
            when {
                alternativeKind == KeymapKind.WIN -> content.append(" ($alternativeShortcut $altText)")
                macKeyStokesFont != null && macKeyStokesFont!!.canDisplayUpTo(alternativeShortcut) == -1 -> {
                    content.append(" (")
                    fragments.add(Pair(content.toString(), null))
                    fragments.add(Pair(alternativeShortcut, macKeyStokesFont))
                    fragments.add(Pair("&nbsp;$altText)", null))
                }
                else -> {
                    val macShortcutAsWin = shortcutText(macKeymap?.getShortcuts(actionId), KeymapKind.WIN)
                    if (macShortcutAsWin.length > 0 && macShortcutAsWin != currentShortcut) {
                        content.append(" ($macShortcutAsWin $altText)")
                    }
                }
            }
        }
        if (fragments.empty) {
            fragments.add(Pair(content.toString(), null))
        }
        if (infoPanel != null) {
            Disposer.dispose(infoPanel!!)
        }
        val realProject = actionData.project ?: ProjectManager.getInstance()!!.getOpenProjects().find { true }
        if (realProject != null) {
            infoPanel = ActionInfoPanel(realProject, fragments)
        }
    }

    private fun shortcutText(shortcuts: Array<Shortcut>?, keymapKind: KeymapKind) =
        when {
            shortcuts == null || shortcuts.size == 0 -> ""
//            win -> shortcutText(shortcuts!![0], win)//todo[nik] why do I need '!!' here
            else -> shortcutText(shortcuts[0], keymapKind)
        }

    private fun shortcutText(shortcut: Shortcut, keymapKind: KeymapKind) =
        when (shortcut) {
            is KeyboardShortcut -> array(shortcut.getFirstKeyStroke(), shortcut.getSecondKeyStroke()).filterNotNull().map { shortcutText(it, keymapKind) }.makeString(separator = ", ")
            else -> ""
        }

    private fun shortcutText(keystroke: KeyStroke, keymapKind: KeymapKind) =
        when (keymapKind) {
            KeymapKind.MAC -> MacKeymapUtil.getKeyStrokeText(keystroke) ?: ""
            KeymapKind.WIN -> {
                val modifiers = keystroke.getModifiers()
                val tokens = array(
                   if (modifiers > 0) getWinModifiersText(modifiers) else null,
                   getWinKeyText(keystroke.getKeyCode())
                )
                tokens.filterNotNull().filter {it.isNotEmpty()}.makeString(separator = "+").trim()
            }
    }

    public fun disable() {
        if (infoPanel != null) {
            Disposer.dispose(infoPanel!!)
            infoPanel = null
        }
        Disposer.dispose(this)
    }

    override fun dispose() {
    }
}