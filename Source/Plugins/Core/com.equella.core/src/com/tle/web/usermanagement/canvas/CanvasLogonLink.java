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
import com.tle.core.institution.InstitutionService;
import com.tle.core.settings.service.ConfigurationService;
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
	private InstitutionService institutionService;
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
			String ssoLink = institutionService.institutionalise("canvassso");
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
