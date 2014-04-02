package org.nik.presentationAssistant;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import org.nik.presentationAssistant.PresentationAssistantState;

/**
 * @author nik
 */
public abstract class PresentationAssistantBase implements ApplicationComponent, PersistentStateComponent<PresentationAssistantState> {
}
