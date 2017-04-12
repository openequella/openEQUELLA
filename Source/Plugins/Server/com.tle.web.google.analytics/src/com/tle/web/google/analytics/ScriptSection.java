package com.tle.web.google.analytics;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.user.CurrentInstitution;
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
