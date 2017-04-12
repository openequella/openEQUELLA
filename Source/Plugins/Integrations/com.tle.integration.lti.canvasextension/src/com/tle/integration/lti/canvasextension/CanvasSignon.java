package com.tle.integration.lti.canvasextension;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.AfterParametersListener;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;

/**
 * Not sign-on as such, but an extension of signon.do which doesn't do sign-on
 * but sets up an integration selection session
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class CanvasSignon extends AbstractPrototypeSection<SingleSignonForm> implements AfterParametersListener
{
	@Inject
	private CanvasIntegration canvasIntegration;

	@Override
	public void afterParameters(SectionInfo info, ParametersEvent event)
	{
		final SingleSignonForm model = getModel(info);
		canvasIntegration.setupSingleSignOn(info, model);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "";
	}

	@Override
	public SingleSignonForm instantiateModel(SectionInfo info)
	{
		return new SingleSignonForm();
	}
}
