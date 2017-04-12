package com.tle.web.wizard.scripting;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.common.scripting.service.ScriptContextCreationParams;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.wizard.WizardState;

/**
 * @author aholland
 */
public interface WizardScriptContextCreationParams extends ScriptContextCreationParams
{
	HTMLControl getControl();

	WizardPage getPage();

	WorkflowStatus getWorkflowStatus();

	WizardState getWizardState();
}
