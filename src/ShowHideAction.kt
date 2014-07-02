/**
 * Created by IntelliJ IDEA.
 * Author: Vladimir Kravets
 * E-Mail: vova.kravets@gmail.com
 * Date: 7/2/14
 * Time: 8:07 PM
 */

package org.nik.presentationAssistant.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import org.nik.presentationAssistant.getPresentationAssistant

public class ShowHideAction : AnAction() {

    override fun actionPerformed(p0: AnActionEvent?) {
        val presentationAssistant = getPresentationAssistant()
        presentationAssistant.setShowActionsDescriptions(!presentationAssistant.configuration.showActionDescriptions)
    }

}