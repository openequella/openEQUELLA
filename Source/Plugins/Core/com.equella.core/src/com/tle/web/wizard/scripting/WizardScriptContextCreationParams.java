/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
