package com.tle.web.integration;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.web.integration.generic.GenericIntegrationService.GenericIntegrationData;
import com.tle.web.integration.guice.IntegrationModule;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;

/**
 * @author jmaginnis
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class SingleSignonAction extends AbstractPrototypeSection<SingleSignonForm>
{
	@Inject
	private IntegrationService integrationService;

	@DirectEvent
	public void execute(SectionContext context) throws Exception
	{
		SectionUtils.dispatchToMethod(getModel(context).getMethod(), this, context);
	}

	public void unimplemented(SectionContext info) throws Exception
	{
		final SingleSignonForm form = getModel(info);
		final String method = form.getMethod();

		final Integration<? extends IntegrationSessionData> integ = integrationService
			.getIntegrationServiceForId(method);

		integ.setupSingleSignOn(info, form);
	}

	@SuppressWarnings("nls")
	public void lms(SectionContext info) throws Exception
	{
		final SingleSignonForm formData = getModel(info);

		final GenericIntegrationData data = new GenericIntegrationData(formData.getTemplate(), formData.getReturnurl(),
			formData.getCancelurl(), formData.getReturnprefix(), info.getRequest().getHeader("Referer"),
			formData.getAction());
		data.setCourseInfoCode(integrationService.getCourseInfoCode(formData.getCourseId(), formData.getCourseCode()));

		// If the caller hasn't specified the action, enforce the fits-all
		// EQUELLA versions default; see #8152
		String formDataAction = formData.getAction();
		if( Check.isEmpty(formDataAction) )
		{
			formDataAction = IntegrationModule.SELECT_OR_ADD_DEFAULT_ACTION;
		}

		final IntegrationActionInfo action = integrationService.getActionInfo(formDataAction, formData.getOptions());
		integrationService.standardForward(info, convertToForward(action, formData), data, action, formData);
	}

	private String convertToForward(IntegrationActionInfo action, SingleSignonForm model)
	{
		String forward = action.getPath();
		if( forward == null )
		{
			forward = action.getName();
		}

		if( action.getName().equals("standard") )
		{
			forward = forward + model.getQuery();
		}

		return forward.substring(1);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "";
	}

	@Override
	public Class<SingleSignonForm> getModelClass()
	{
		return SingleSignonForm.class;
	}
}
