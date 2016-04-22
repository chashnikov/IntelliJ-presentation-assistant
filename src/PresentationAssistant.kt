/**
 * @author nik
 */
package org.nik.presentationAssistant

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.openapi.options.Configurable
import javax.swing.JCheckBox
import com.intellij.util.ui.FormBuilder
import com.intellij.openapi.application.ApplicationManager
import javax.swing.JPanel
import com.intellij.openapi.options.SearchableConfigurable
import java.awt.BorderLayout
import javax.swing.JTextField
import com.intellij.util.ui.UIUtil
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.ui.ListCellRendererWrapper
import com.intellij.openapi.keymap.Keymap
import javax.swing.JList
import com.intellij.openapi.keymap.KeymapManager

public class PresentationAssistantState {
    var showActionDescriptions = true
    var fontSize = 24
    var mainKeymap = getDefaultMainKeymap()
    var alternativeKeymap = getDefaultAlternativeKeymap()
}

//todo[nik] report kotlin bug: if PersistentStateComponent is implemented directly IDEA is unable to obtain state class
@State(name = "PresentationAssistant", storages = arrayOf(Storage(file = "${StoragePathMacros.APP_CONFIG}/presentation-assistant.xml")))
public class PresentationAssistant : PresentationAssistantBase() {
    val configuration = PresentationAssistantState()
    var presenter: ShortcutPresenter? = null

    override fun getState() = configuration
    override fun loadState(p0: PresentationAssistantState?) {
        XmlSerializerUtil.copyBean(p0!!, configuration)
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

    fun setShowActionsDescriptions(value: Boolean) {
        configuration.showActionDescriptions = value
        if (value && presenter == null) {
            presenter = ShortcutPresenter()
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

fun getPresentationAssistant(): PresentationAssistant = ApplicationManager.getApplication()!!.getComponent(PresentationAssistant::class.java)!!

public class KeymapDescriptionPanel {
    val combobox : ComboBox
    val text = JTextField(10)
    val mainPanel: JPanel
    init
    {
        combobox = ComboBox(KeymapManagerEx.getInstanceEx()!!.getAllKeymaps()!!)
        combobox.setRenderer(object: ListCellRendererWrapper<Keymap>() {
            override fun customize(jList: JList<*>?, t: Keymap?, i: Int, b: Boolean, b1: Boolean) {
                setText(t?.getPresentableName() ?: "")
            }
        })
        val formBuilder = FormBuilder.createFormBuilder()!!
                .setIndent(20)!!
                .addLabeledComponent("Keymap:", combobox)!!
                .addLabeledComponent("Description:", text)!!
        mainPanel = formBuilder.getPanel()!!
    }

    fun getDescription() = KeymapDescription((combobox.getSelectedItem() as Keymap?)?.getName() ?: "", text.getText()!!)

    fun setEnabled(enabled: Boolean) {
        UIUtil.setEnabled(mainPanel, enabled, true)
    }

    fun reset(config: KeymapDescription) {
        combobox.setSelectedItem(KeymapManager.getInstance()!!.getKeymap(config.name))
        text.setText(config.displayText)
    }
}

public class PresentationAssistantConfigurable : Configurable, SearchableConfigurable {
    val configuration: PresentationAssistant = getPresentationAssistant()
    val showActionsCheckbox = JCheckBox(UIUtil.replaceMnemonicAmpersand("&Show action names and shortcuts"))
    val showAltKeymap = JCheckBox("Alternative Keymap:")
    val mainKeymapPanel = KeymapDescriptionPanel()
    val altKeymapPanel = KeymapDescriptionPanel()
    val fontSizeField = JTextField(5)
    val mainPanel: JPanel
    init
    {
        val formBuilder = FormBuilder.createFormBuilder()!!
                           .addComponent(showActionsCheckbox)!!
                           .addLabeledComponent("&Font size:", fontSizeField)!!
                           .addVerticalGap(10)!!
                           .addLabeledComponent("Main Keymap:", mainKeymapPanel.mainPanel, true)!!
                           .addLabeledComponent(showAltKeymap, altKeymapPanel.mainPanel, true)!!
        showActionsCheckbox.addActionListener { updatePanels() }
        showAltKeymap.addActionListener {
            altKeymapPanel.setEnabled(showAltKeymap.isSelected() && showActionsCheckbox.isSelected())
        }
        mainPanel = JPanel(BorderLayout())
        mainPanel.add(BorderLayout.NORTH, formBuilder.getPanel()!!)
    }

    private fun updatePanels() {
        val enabled = showActionsCheckbox.isSelected()
        fontSizeField.setEnabled(enabled)
        showAltKeymap.setEnabled(enabled)
        mainKeymapPanel.setEnabled(enabled)
        altKeymapPanel.setEnabled(enabled && showAltKeymap.isSelected())
    }

    override fun getId() = getDisplayName()
    override fun enableSearch(p0: String?) = null
    override fun getDisplayName() = "Presentation Assistant"
    override fun getHelpTopic(): String? = null

    override fun createComponent( )= mainPanel
    override fun isModified() = showActionsCheckbox.isSelected() != configuration.configuration.showActionDescriptions
                                || fontSizeField.getText() != configuration.configuration.fontSize.toString()
                                || configuration.configuration.mainKeymap != mainKeymapPanel.getDescription()
                                || configuration.configuration.alternativeKeymap != getAlternativeKeymap()

    fun getAlternativeKeymap() = if (showAltKeymap.isSelected()) altKeymapPanel.getDescription() else null

    override fun apply() {
        configuration.setShowActionsDescriptions(showActionsCheckbox.isSelected())
        configuration.setFontSize(fontSizeField.getText()!!.trim().toInt())
        configuration.configuration.mainKeymap = mainKeymapPanel.getDescription()
        configuration.configuration.alternativeKeymap = getAlternativeKeymap()

    }
    override fun reset() {
        showActionsCheckbox.setSelected(configuration.configuration.showActionDescriptions)
        fontSizeField.setText(configuration.configuration.fontSize.toString())
        showAltKeymap.setSelected(configuration.configuration.alternativeKeymap != null)
        mainKeymapPanel.reset(configuration.configuration.mainKeymap)
        altKeymapPanel.reset(configuration.configuration.alternativeKeymap ?: KeymapDescription("", ""))
        updatePanels()
    }
    override fun disposeUIResources() {
    }
}