package com.tle.web.usermanagement.canvas;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.usermanagement.canvas.CanvasWrapperSettings;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.login.LoginLink;
import com.tle.web.login.LogonSection.LogonModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class CanvasLogonLink extends AbstractPrototypeSection<CanvasLogonLink.CanvasLogonModel> implements LoginLink
{
	@Inject
	private UrlService urlService;
	@Inject
	private ConfigurationService configurationService;

	@PlugKey("link.login")
	@Component
	private Link link;

	@Override
	public void setup(RenderEventContext context, LogonModel model)
	{
		if( isEnabled(context) )
		{
			final String page = model.getPage();
			String ssoLink = urlService.institutionalise("canvassso");
			if( !Strings.isNullOrEmpty(page) )
			{
				try
				{
					ssoLink = ssoLink + "?page=" + URLEncoder.encode(page, "UTF-8");
				}
				catch( UnsupportedEncodingException e )
				{
					throw Throwables.propagate(e);
				}
			}
			link.setBookmark(context, new SimpleBookmark(ssoLink));
		}
	}

	@Nullable
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( isEnabled(context) )
		{
			return new LinkRenderer(link.getState(context));
		}
		return null;
	}

	private boolean isEnabled(SectionInfo info)
	{
		final CanvasLogonModel model = getModel(info);
		Boolean enabled = model.getEnabled();

		if( enabled == null )
		{
			CanvasWrapperSettings settings = configurationService.getProperties(new CanvasWrapperSettings());
			enabled = settings.isEnabled();
			model.setEnabled(enabled);
		}
		return enabled;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new CanvasLogonModel();
	}

	@NonNullByDefault(false)
	public static class CanvasLogonModel
	{
		private Boolean enabled;

		public Boolean getEnabled()
		{
			return enabled;
		}

		public void setEnabled(Boolean enabled)
		{
			this.enabled = enabled;
		}
	}
}
