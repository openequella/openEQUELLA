package com.tle.web.controls.mypages;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.tle.core.guice.BindFactory;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.mypages.service.MyPagesService;
import com.tle.web.wizard.WizardState;

public class EnsureItemUrls extends AbstractWorkflowOperation
{
	@Inject
	private MyPagesService myPagesService;
	private final WizardState state;

	@Inject
	public EnsureItemUrls(@Assisted WizardState state)
	{
		this.state = state;
	}

	@Override
	public boolean execute()
	{
		myPagesService.convertPreviewUrlsToItemUrls(state);
		return false;
	}

	public void setMyPagesService(MyPagesService myPagesService)
	{
		this.myPagesService = myPagesService;
	}

	@BindFactory
	public interface EnsureFactory
	{
		EnsureItemUrls create(WizardState state);
	}
}
