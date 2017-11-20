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

package com.tle.web.google.analytics;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.common.institution.CurrentInstitution;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.GenericNamedResult;
import com.tle.web.sections.render.HtmlRenderer;

@Bind
@SuppressWarnings("nls")
public class ScriptSection extends AbstractPrototypeSection<ScriptSection.ScriptSectionModel> implements HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private ConfigurationService configService;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		ScriptSectionModel model = getModel(context);
		if( !checkGoogleConfiguration(model) || context.getAttributeForClass(AjaxRenderContext.class) != null )
		{
			return null;
		}
		return new GenericNamedResult("body", viewFactory.createResult("googlescript.ftl", context));
	}

	private boolean checkGoogleConfiguration(ScriptSectionModel model)
	{
		if( CurrentInstitution.get() == null )
		{
			return false;
		}

		final String gaid = configService.getProperty(GoogleAnalyticsPage.ANALYTICS_KEY);
		boolean b = !Check.isEmpty(gaid);
		if( b )
		{
			model.setGoogleAccountId(gaid);
		}
		return b;
	}

	@Override
	public Class<ScriptSectionModel> getModelClass()
	{
		return ScriptSection.ScriptSectionModel.class;
	}

	public static class ScriptSectionModel
	{
		private String googleAccountId;

		public String getGoogleAccountId()
		{
			return googleAccountId;
		}

		public void setGoogleAccountId(String googleAccountId)
		{
			this.googleAccountId = googleAccountId;
		}
	}
}
