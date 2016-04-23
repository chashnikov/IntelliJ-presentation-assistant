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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ListCellRendererWrapper
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil
import com.intellij.util.xmlb.XmlSerializerUtil
import java.awt.BorderLayout
import javax.swing.JCheckBox
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTextField

class PresentationAssistantState {
    var showActionDescriptions = true
    var fontSize = 24
    var mainKeymap = getDefaultMainKeymap()
    var alternativeKeymap = getDefaultAlternativeKeymap()
}

@State(name = "PresentationAssistant", storages = arrayOf(Storage(file = "presentation-assistant.xml")))
class PresentationAssistant : ApplicationComponent, PersistentStateComponent<PresentationAssistantState> {
    val configuration = PresentationAssistantState()
    var presenter: ShortcutPresenter? = null

    override fun getState() = configuration
    override fun loadState(p: PresentationAssistantState) {
        XmlSerializerUtil.copyBean(p, configuration)
    }
    override fun initComponent() {
        if (configuration.showActionDescriptions) {
            presenter = ShortcutPresenter()
        }
    }
    override fun disposeComponent() {
        presenter?.disable()
    }
    override fun getComponentName() = "PresentationAssistant"

    fun setShowActionsDescriptions(value: Boolean, project: Project?) {
        configuration.showActionDescriptions = value
        if (value && presenter == null) {
            presenter = ShortcutPresenter().apply {
                showActionInfo(ShortcutPresenter.ActionData("presentationAssistant.ShowActionDescriptions", project, "Show Descriptions of Actions"))
            }
        }
        if (!value && presenter != null) {
            presenter?.disable()
            presenter = null
        }
    }

    fun setFontSize(value: Int) {
        configuration.fontSize = value
    }
}

fun getPresentationAssistant(): PresentationAssistant = ApplicationManager.getApplication().getComponent(PresentationAssistant::class.java)

class KeymapDescriptionPanel {
    val combobox : ComboBox
    val text = JTextField(10)
    val mainPanel: JPanel
    init
    {
        combobox = ComboBox(KeymapManagerEx.getInstanceEx().allKeymaps)
        combobox.renderer = object: ListCellRendererWrapper<Keymap>() {
            override fun customize(list: JList<*>, t: Keymap?, index: Int, selected: Boolean, hasFocus: Boolean) {
                setText(t?.presentableName ?: "")
            }
        }
        val formBuilder = FormBuilder.createFormBuilder()
                .setFormLeftIndent(20)
                .addLabeledComponent("Keymap:", combobox)
                .addLabeledComponent("Description:", text)
        mainPanel = formBuilder.panel
    }

    fun getDescription() = KeymapDescription((combobox.selectedItem as Keymap?)?.name ?: "", text.text)

    fun setEnabled(enabled: Boolean) {
        UIUtil.setEnabled(mainPanel, enabled, true)
    }

    fun reset(config: KeymapDescription) {
        combobox.selectedItem = KeymapManager.getInstance().getKeymap(config.name)
        text.text = config.displayText
    }
}

class PresentationAssistantConfigurable : Configurable, SearchableConfigurable {
    val configuration: PresentationAssistant = getPresentationAssistant()
    val showAltKeymap = JCheckBox("Alternative Keymap:")
    val mainKeymapPanel = KeymapDescriptionPanel()
    val altKeymapPanel = KeymapDescriptionPanel()
    val fontSizeField = JTextField(5)
    val mainPanel: JPanel
    init
    {
        val formBuilder = FormBuilder.createFormBuilder()
                           .addLabeledComponent("&Font size:", fontSizeField)
                           .addVerticalGap(10)
                           .addLabeledComponent("Main Keymap:", mainKeymapPanel.mainPanel, true)
                           .addLabeledComponent(showAltKeymap, altKeymapPanel.mainPanel, true)
        showAltKeymap.addActionListener {
            altKeymapPanel.setEnabled(showAltKeymap.isSelected)
        }
        mainPanel = JPanel(BorderLayout())
        mainPanel.add(BorderLayout.NORTH, formBuilder.panel)
    }

    private fun updatePanels() {
        altKeymapPanel.setEnabled(showAltKeymap.isSelected)
    }

    override fun getId() = displayName
    override fun enableSearch(option: String?) = null
    override fun getDisplayName() = "Presentation Assistant"
    override fun getHelpTopic() = null

    override fun createComponent() = mainPanel
    override fun isModified() = fontSizeField.text != configuration.configuration.fontSize.toString()
                                || configuration.configuration.mainKeymap != mainKeymapPanel.getDescription()
                                || configuration.configuration.alternativeKeymap != getAlternativeKeymap()

    fun getAlternativeKeymap() = if (showAltKeymap.isSelected) altKeymapPanel.getDescription() else null

    override fun apply() {
        configuration.setFontSize(fontSizeField.text.trim().toInt())
        configuration.configuration.mainKeymap = mainKeymapPanel.getDescription()
        configuration.configuration.alternativeKeymap = getAlternativeKeymap()

    }
    override fun reset() {
        fontSizeField.text = configuration.configuration.fontSize.toString()
        showAltKeymap.isSelected = configuration.configuration.alternativeKeymap != null
        mainKeymapPanel.reset(configuration.configuration.mainKeymap)
        altKeymapPanel.reset(configuration.configuration.alternativeKeymap ?: KeymapDescription("", ""))
        updatePanels()
    }
    override fun disposeUIResources() {
    }
}