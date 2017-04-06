package com.tle.web.usermanagement.autoip;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.system.AutoLogin;
import com.tle.core.guice.Bind;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class AutoIpLogonSection extends AbstractPrototypeSection<AutoIpLogonSection.Model> implements HtmlRenderer
{
	@Component
	private Link autoLogonLink;
	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private AutoIpLogonService autoIpLogonService;
	@Inject
	private ConfigurationService configService;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		autoLogonLink.setClickHandler(events.getNamedHandler("autoIPLogon"));
	}

	public Link getAutoLogonLink()
	{
		return autoLogonLink;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Model model = getModel(context);
		AutoLogin autoLogin = configService.getProperties(new AutoLogin());
		if( configService.isAutoLoginAvailable(autoLogin) )
		{
			model.setAutoUsername(autoLogin.getUsername());
			return viewFactory.createResult("autoiplogon.ftl", this);
		}
		return null;
	}

	/**
	 * @param info
	 * @return true if the Auto IP Login is enabled, appropriate and successful
	 */
	@EventHandlerMethod(preventXsrf = false)
	public void autoIPLogon(SectionInfo info)
	{
		autoIpLogonService.autoLogon(info.getRequest());
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model
	{
		private String autoUsername;

		public String getAutoUsername()
		{
			return autoUsername;
		}

		public void setAutoUsername(String autoUsername)
		{
			this.autoUsername = autoUsername;
		}
	}
}
