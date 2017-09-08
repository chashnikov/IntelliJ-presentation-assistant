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

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.keymap.MacKeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.SystemInfo
import java.awt.Font
import java.util.*
import javax.swing.KeyStroke

class ShortcutPresenter : Disposable {
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
    private var infoPanel: ActionInfoPanel? = null
    private val parentNames = HashMap<String, String>();
    init
    {
        enable()
    }

    private fun fillParentNames(group: ActionGroup, parentName: String) {
        val actionManager = ActionManager.getInstance()
        for (item in group.getChildren(null)) {
            when (item) {
                is ActionGroup -> {
                    if (!item.isPopup) fillParentNames(item, parentName)
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
        val actionManager = ActionManager.getInstance()
        for (groupId in parentGroupIds) {
            val group = actionManager.getAction(groupId)
            if (group is ActionGroup) {
                fillParentNames(group, group.getTemplatePresentation().text!!)
            }
        }

        actionManager.addAnActionListener(object: AnActionListener {
            var currentAction: ActionData? = null

            override fun beforeActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent?) {
                currentAction = null
                val actionId = ActionManager.getInstance().getId(action) ?: return

                if (!movingActions.contains(actionId) && !typingActions.contains(actionId) && event != null) {
                    val project = event.project
                    val text = event.presentation.text
                    currentAction = ActionData(actionId, project, text)
                }
            }

            override fun afterActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent?) {
                val actionData = currentAction
                val actionId = ActionManager.getInstance().getId(action)
                if (actionData != null && actionData.actionId == actionId) {
                    showActionInfo(actionData)
                }
            }

            override fun beforeEditorTyping(c: Char, dataContext: DataContext?) {}
        }, this)
    }

    class ActionData(val actionId: String, val project: Project?, val actionText: String?)

    private fun MutableList<Pair<String, Font?>>.addText(text: String) {
        this.add(Pair(text, null))
    }

    fun showActionInfo(actionData: ActionData) {
        val actionId = actionData.actionId
        val parentGroupName = parentNames[actionId]
        val actionText = (if (parentGroupName != null) "$parentGroupName ${MacKeymapUtil.RIGHT} " else "") + (actionData.actionText ?: "").removeSuffix("...")

        val fragments = ArrayList<Pair<String, Font?>>()
        if (actionText.isNotEmpty()) {
            fragments.addText("<b>$actionText</b>")
        }

        val mainKeymap = getPresentationAssistant().configuration.mainKeymap
        val shortcutTextFragments = shortcutTextFragments(mainKeymap, actionId, actionText)
        if (shortcutTextFragments.isNotEmpty()) {
            if (fragments.isNotEmpty()) fragments.addText(" via&nbsp;")
            fragments.addAll(shortcutTextFragments)
        }

        val alternativeKeymap = getPresentationAssistant().configuration.alternativeKeymap
        if (alternativeKeymap != null) {
            val mainShortcut = shortcutText(mainKeymap.getKeymap()?.getShortcuts(actionId), mainKeymap.getKind())
            val altShortcutTextFragments = shortcutTextFragments(alternativeKeymap, actionId, mainShortcut)
            if (altShortcutTextFragments.isNotEmpty()) {
                fragments.addText("&nbsp;(");
                fragments.addAll(altShortcutTextFragments)
                fragments.addText(")");
            }
        }

        val realProject = actionData.project ?: ProjectManager.getInstance().openProjects.firstOrNull()
        if (realProject != null && !realProject.isDisposed && realProject.isOpen) {
            if (infoPanel == null || !infoPanel!!.canBeReused()) {
                infoPanel = ActionInfoPanel(realProject, fragments)
            } else {
                infoPanel!!.updateText(realProject, fragments)
            }
        }
    }

    private fun shortcutTextFragments(keymap: KeymapDescription, actionId: String, shownShortcut: String): List<Pair<String, Font?>> {
        val fragments = ArrayList<Pair<String, Font?>>()
        val shortcutText = shortcutText(keymap.getKeymap()?.getShortcuts(actionId), keymap.getKind())
        if (shortcutText.isEmpty() || shortcutText == shownShortcut) return fragments

        when {
            keymap.getKind() == KeymapKind.WIN || SystemInfo.isMac -> {
                fragments.addText(shortcutText)
            }
            macKeyStrokesFont != null && macKeyStrokesFont!!.canDisplayUpTo(shortcutText) == -1 -> {
                fragments.add(Pair(shortcutText, macKeyStrokesFont))
            }
            else -> {
                val altShortcutAsWin = shortcutText(keymap.getKeymap()?.getShortcuts(actionId), KeymapKind.WIN)
                if (altShortcutAsWin.isNotEmpty() && shownShortcut != altShortcutAsWin) {
                    fragments.addText(altShortcutAsWin)
                }
            }
        }
        val keymapText = keymap.displayText
        if (keymapText.isNotEmpty()) {
            fragments.addText("&nbsp;$keymapText")
        }
        return fragments
    }

    private fun shortcutText(shortcuts: Array<Shortcut>?, keymapKind: KeymapKind) =
        when {
            shortcuts == null || shortcuts.isEmpty() -> ""
            else -> shortcutText(shortcuts[0], keymapKind)
        }

    private fun shortcutText(shortcut: Shortcut, keymapKind: KeymapKind) =
        when (shortcut) {
            is KeyboardShortcut -> arrayOf(shortcut.firstKeyStroke, shortcut.secondKeyStroke).filterNotNull().map { shortcutText(it, keymapKind) }.joinToString(separator = ", ")
            else -> ""
        }

    private fun shortcutText(keystroke: KeyStroke, keymapKind: KeymapKind) =
        when (keymapKind) {
            KeymapKind.MAC -> MacKeymapUtil.getKeyStrokeText(keystroke) ?: ""
            KeymapKind.WIN -> {
                val modifiers = keystroke.modifiers
                val tokens = arrayOf(
                   if (modifiers > 0) getWinModifiersText(modifiers) else null,
                   getWinKeyText(keystroke.keyCode)
                )
                tokens.filterNotNull().filter { it.isNotEmpty() }.joinToString(separator = "+").trim()
            }
    }

    fun disable() {
        if (infoPanel != null) {
            infoPanel!!.close()
            infoPanel = null
        }
        Disposer.dispose(this)
    }

    override fun dispose() {
    }
}