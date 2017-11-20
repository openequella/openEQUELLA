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
			formData.getAction(), true);
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
