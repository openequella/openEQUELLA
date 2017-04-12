package com.tle.web.template.section;

import com.google.inject.Inject;
import com.tle.core.system.SystemConfigService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.GenericNamedResult;
import com.tle.web.sections.render.HtmlRenderer;

@SuppressWarnings("nls")
public class ServerMessageSection extends AbstractPrototypeSection<ServerMessageSection.ServerMessageModel>
	implements
		HtmlRenderer
{
	@Inject
	private SystemConfigService systemConfigService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !systemConfigService.isSystemSchemaUp() || !systemConfigService.isServerMessageEnabled() )
		{
			return null;
		}

		getModel(context).setServerMessage(systemConfigService.getServerMessage());
		return new GenericNamedResult("servermessage", viewFactory.createResult("servermessage.ftl", context));
	}

	@Override
	public Class<ServerMessageModel> getModelClass()
	{
		return ServerMessageModel.class;
	}

	public static class ServerMessageModel
	{
		private String serverMessage;

		public String getServerMessage()
		{
			return serverMessage;
		}

		public void setServerMessage(String serverMessage)
		{
			this.serverMessage = serverMessage;
		}
	}
}
