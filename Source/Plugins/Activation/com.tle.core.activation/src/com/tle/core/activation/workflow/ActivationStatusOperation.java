package com.tle.core.activation.workflow;

import javax.inject.Inject;

import com.tle.core.activation.service.ActivationService;
import com.tle.core.guice.Bind;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;

@Bind
public class ActivationStatusOperation extends AbstractStandardWorkflowOperation
{
	@Inject
	private ActivationService activationService;

	@Override
	public boolean execute()
	{
		activationService.ensureStatesForItem(getItem());
		return true;
	}
}
