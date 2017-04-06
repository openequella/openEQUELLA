package com.tle.web.controls.mypages;

import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.workflow.operations.DuringSaveOperation;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.web.controls.mypages.EnsureItemUrls.EnsureFactory;
import com.tle.web.wizard.WizardState;

public class ChangePreviewUrls extends FactoryMethodLocator<WorkflowOperation> implements DuringSaveOperation
{
	private static final long serialVersionUID = 1L;

	public ChangePreviewUrls(WizardState state)
	{
		super(EnsureFactory.class, "create", state); //$NON-NLS-1$
	}

	@Override
	public WorkflowOperation createPostSaveWorkflowOperation()
	{
		return null;
	}

	@Override
	public WorkflowOperation createPreSaveWorkflowOperation()
	{
		return get();
	}

	@Override
	public String getName()
	{
		return null;
	}
}
