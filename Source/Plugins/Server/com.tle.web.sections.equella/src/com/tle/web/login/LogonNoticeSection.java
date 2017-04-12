package com.tle.web.login;

import javax.inject.Inject;

import com.dytech.edge.web.WebConstants;
import com.tle.beans.system.AutoLogin;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.header.BodyTag;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.GenericNamedResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.template.Decorations;

@Bind
public class LogonNoticeSection extends AbstractPrototypeSection<LogonNoticeSection.LoginNoticeModel>
	implements
		HtmlRenderer
{
	@PlugKey("logonnotice.title")
	private static Label TITLE_LABEL;

	@Inject
	private ConfigurationService configService;
	@Inject
	private UserPreferenceService userPreferenceService;
	@Inject
	private UserSessionService userSessionService;
	@Inject
	private UrlService urlService;
	@EventFactory
	private EventGenerator events;

	@Override
	public String getDefaultPropertyName()
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		String notice = getModel(context).getNotice();
		BodyTag body = context.getBody();
		if( !Check.isEmpty(notice) )
		{
			body.addReadyStatements(events.getSubmitValuesHandler(
				"confirmed", new FunctionCallExpression(Confirm.CONFIRM, new TextLabel(notice)))); //$NON-NLS-1$
		}
		else
		{
			body.addReadyStatements(events.getSubmitValuesHandler("confirmed", true)); //$NON-NLS-1$
		}

		Decorations decs = Decorations.getDecorations(context);
		decs.clearAllDecorations();
		decs.setTitle(TITLE_LABEL);
		return new GenericNamedResult("pagetitle", new LabelRenderer(TITLE_LABEL)); //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void checkForNotices(SectionInfo info) throws Exception
	{
		final boolean hideNotice = userPreferenceService.isHideLoginNotice();
		String loginNotice = configService.getProperties(new AutoLogin()).getLoginNotice();
		final String notice = Check.nullToEmpty(hideNotice ? null : loginNotice).replaceAll("\\\\n", "\n");

		getModel(info).setNotice(notice);

		if( Check.isEmpty(notice) )
		{
			confirmed(info, true);
			return;
		}
	}

	@EventHandlerMethod
	public void confirmed(SectionInfo info, boolean confirm) throws Exception
	{
		String homepage;
		if( confirm )
		{
			homepage = userSessionService.getAttribute(WebConstants.PAGE_AFTER_LOGON_KEY);
			if( homepage != null )
			{
				userSessionService.removeAttribute(WebConstants.PAGE_AFTER_LOGON_KEY);
			}
			if( homepage == null )
			{
				homepage = info.getTreeAttribute("HOMEPAGE"); //$NON-NLS-1$
			}
			if( homepage == null )
			{
				homepage = WebConstants.DEFAULT_HOME_PAGE;
			}

			// If homepage is not a relative URL, see if it's an institution
			// URL, if
			// it isn't then we refuse to use it.
			if( URLUtils.isAbsoluteUrl(homepage) )
			{
				if( !urlService.isInstitutionUrl(homepage) )
				{
					homepage = WebConstants.DEFAULT_HOME_PAGE;
				}
			}
		}
		else
		{
			homepage = "logon.do?logout=true"; //$NON-NLS-1$
		}

		info.forwardToUrl(urlService.institutionalise(homepage));
	}

	public static class LoginNoticeModel
	{
		private String notice;

		public String getNotice()
		{
			return notice;
		}

		public void setNotice(String notice)
		{
			this.notice = notice;
		}
	}

	@Override
	public Class<LoginNoticeModel> getModelClass()
	{
		return LoginNoticeModel.class;
	}
}
