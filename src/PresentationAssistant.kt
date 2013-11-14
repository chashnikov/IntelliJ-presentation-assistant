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

public class PresentationAssistantState {
    var showActionDescriptions = true
    var fontSize = 24
}

//todo[nik] report kotlin bug: if PersistentStateComponent is implemented directly IDEA is unable to obtain state class
//todo[nik] report kotlin bug: it seems that expressions aren't evaluated in annotations
//State(name = "PresentationAssistant", storages = array(Storage(file = "${StoragePathMacros.APP_CONFIG}/presentation-assistant.xml")))
State(name = "PresentationAssistant", storages = array(Storage(file = "\$APP_CONFIG\$/presentation-assistant.xml")))
public class PresentationAssistant : PresentationAssistantBase() {
    //todo[nik] report kotlin bug: val state and getState() results in duplicated method exception
    val configuration = PresentationAssistantState()
    var presenter: ShortcutPresenter? = null

    override fun getState() = configuration
    override fun loadState(p0: PresentationAssistantState?) {
        XmlSerializerUtil.copyBean(p0, configuration)
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

fun getPresentationAssistant(): PresentationAssistant = ApplicationManager.getApplication()!!.getComponent(javaClass<PresentationAssistant>())!!

public class PresentationAssistantConfigurable : Configurable, SearchableConfigurable {
    val configuration: PresentationAssistant = getPresentationAssistant()
    val showActionsCheckbox = JCheckBox(UIUtil.replaceMnemonicAmpersand("&Show action names and shortcuts"))
    val fontSizeField = JTextField(5)
    val mainPanel: JPanel
    {
        val formBuilder = FormBuilder.createFormBuilder()!!
                           .addComponent(showActionsCheckbox)!!
                           .addLabeledComponent("&Font size:", fontSizeField)!!
        showActionsCheckbox.addActionListener { fontSizeField.setEnabled(showActionsCheckbox.isSelected()) }
        mainPanel = JPanel(BorderLayout())
        mainPanel.add(BorderLayout.NORTH, formBuilder.getPanel()!!)
    }


    override fun getId() = getDisplayName()
    override fun enableSearch(p0: String?) = null
    override fun getDisplayName() = "Presentation Assistant"
    override fun getHelpTopic(): String? = null

    override fun createComponent( )= mainPanel
    override fun isModified() = showActionsCheckbox.isSelected() != configuration.configuration.showActionDescriptions
                                || fontSizeField.getText() != configuration.configuration.fontSize.toString()
    override fun apply() {
        configuration.setShowActionsDescriptions(showActionsCheckbox.isSelected())
        configuration.setFontSize(fontSizeField.getText()!!.trim().toInt())
    }
    override fun reset() {
        showActionsCheckbox.setSelected(configuration.configuration.showActionDescriptions)
        fontSizeField.setText(configuration.configuration.fontSize.toString())
        fontSizeField.setEnabled(showActionsCheckbox.isSelected())
    }
    override fun disposeUIResources() {
    }
}