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

package com.tle.web.wizard.section.model;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.workflow.WorkflowStep;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.services.user.UserService;
import com.tle.web.sections.render.SectionRenderable;

public class WorkflowStepDisplay
{
	private final LanguageBundle displayName;
	private final LanguageBundle description;
	private SectionRenderable assign;
	private UserBean assignedTo;

	public WorkflowStepDisplay(WorkflowStep step, UserService userService)
	{
		this.displayName = step.getDisplayName();
		this.description = step.getDescription();
		this.assignedTo = null;
		if( step.getAssignedTo() != null )
		{
			assignedTo = userService.getInformationForUser(step.getAssignedTo());
		}
	}

	public UserBean getAssignedTo()
	{
		return assignedTo;
	}

	public LanguageBundle getDescription()
	{
		return description;
	}

	public LanguageBundle getDisplayName()
	{
		return displayName;
	}

	public SectionRenderable getAssign()
	{
		return assign;
	}

	public void setAssign(SectionRenderable assign)
	{
		this.assign = assign;
	}

}
