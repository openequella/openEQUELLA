package com.tle.web.wizard.section.model;

import com.dytech.edge.common.valuebean.UserBean;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.workflow.WorkflowStep;
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
