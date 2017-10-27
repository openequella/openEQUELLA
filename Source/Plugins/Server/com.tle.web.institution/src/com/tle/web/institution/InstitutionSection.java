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

package com.tle.web.institution;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;
import com.tle.beans.Institution;
import com.tle.core.filesystem.CustomisationFile;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.ApplicationVersion;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.user.UserSessionService;
import com.tle.common.usermanagement.user.AnonymousUserState;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.DebugSettings;
import com.tle.web.institution.section.ShowInstitutionsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.registry.handler.CollectInterfaceHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.MenuMode;

@SuppressWarnings("nls")
public class InstitutionSection extends AbstractPrototypeSection<InstitutionSection.InstitutionModel>
		implements
		HtmlRenderer,
		ParametersEventListener
{
	@PlugKey("institutions.admin.title")
	private static Label TITLE_LABEL;

	@Inject
	private InstitutionService institutionService;
	@Inject
	private FileSystemService fileSystemService;

	@Inject
	private ShowInstitutionsSection showInstitutionsSection;
	@Inject
	private LogonMigrateSection logonMigrateSection;
	@TreeLookup
	private AutoTestSetupSection autoTestSection;
	@Inject
	private UserSessionService sessionService;

	@EventFactory
	private EventGenerator events;

	private CollectInterfaceHandler<Tabable> tabInterfaces;

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new InstitutionModel();
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations d = Decorations.getDecorations(context);
		d.setTitle(TITLE_LABEL);
		d.setHelp(false);
		d.setOptions(false);
		InstitutionModel model = getModel(context);
		if( logonMigrateSection.shouldShow(context, model.isAdmin()) )
		{
			d.setMenuMode(MenuMode.HIDDEN);
			model.setAdmin(true);
			return renderSection(context, logonMigrateSection);
		}
		if( !model.isAdmin() )
		{
			return renderSection(context, showInstitutionsSection);
		}
		if( model.isAutotest() && DebugSettings.isAutoTestMode() )
		{
			d.setMenuMode(MenuMode.HIDDEN);
			return renderSection(context, autoTestSection);
		}
		return renderFirstResult(context);
	}

	public static class InstitutionModel
	{
		@Bookmarked
		private boolean admin;
		@Bookmarked
		private boolean credits;
		@Bookmarked
		private boolean autotest;

		public boolean isAdmin()
		{
			return admin;
		}

		public void setAdmin(boolean admin)
		{
			this.admin = admin;
		}

		public boolean isCredits()
		{
			return credits;
		}

		public void setCredits(boolean credits)
		{
			this.credits = credits;
		}

		public boolean isAutotest()
		{
			return autotest;
		}

		public void setAutotest(boolean autotest)
		{
			this.autotest = autotest;
		}
	}

	public static class TabDisplay
	{
		private HtmlLinkState link;
		private String clazz;

		public HtmlLinkState getLink()
		{
			return link;
		}

		public void setLink(HtmlLinkState link)
		{
			this.link = link;
		}

		public String getClazz()
		{
			return clazz;
		}

		public void setClazz(String clazz)
		{
			this.clazz = clazz;
		}
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(showInstitutionsSection, id);
		tree.registerInnerSection(logonMigrateSection, id);
		tabInterfaces = new CollectInterfaceHandler<Tabable>(Tabable.class);
		tree.addRegistrationHandler(tabInterfaces);
	}

	public CollectInterfaceHandler<Tabable> getTabInterfaces()
	{
		return tabInterfaces;
	}

	public String getBadgeUrl(SectionInfo info, long instId)
	{
		return new BookmarkAndModify(info, events.getNamedModifier("badge", Long.toString(instId))).getHref();
	}

	@EventHandlerMethod(preventXsrf = false)
	public void badge(SectionInfo info, long instId) throws Exception
	{
		HttpServletResponse response = info.getResponse();
		response.setContentType("image/jpeg");
		try( InputStream in = getBadgeStream(instId); ServletOutputStream out = response.getOutputStream() )
		{
			if( in != null )
			{
				ByteStreams.copy(in, out);
			}
		}
		finally
		{
			info.setRendered();
		}
	}

	private InputStream getBadgeStream(long instId) throws IOException
	{
		final String badgePath = "images/Badge.jpg";
		Institution institution = institutionService.getInstitution(instId);
		if( institution != null )
		{
			final CustomisationFile handle = new CustomisationFile(institution);
			if( fileSystemService.fileExists(handle, "/" + badgePath) )
			{
				return fileSystemService.read(handle, "/" + badgePath);
			}
		}
		return this.getClass().getResourceAsStream("/defaultBadge.jpg");
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void checkSystem(SectionInfo info)
	{
		UserState state = CurrentUser.getUserState();
		if( getModel(info).isAdmin() && (!state.isSystem() || !state.isAuthenticated()) )
		{
			info.renderNow();
		}
		if( CurrentInstitution.get() != null )
		{
			throw new AccessDeniedException("Only admin site access");
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "is";
	}

	@Override
	public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception
	{
		InstitutionModel model = getModel(info);
		String method = event.getParameter("method", false);
		if( method != null )
		{
			model.setAdmin("admin".equals(method));
			model.setCredits("credits".equals(method));
			if( "logout".equals(method) )
			{
				SectionUtils.clearModel(info, this);
				model.setAdmin(false);
				AnonymousUserState userState = new AnonymousUserState();
				userState.setSessionID(UUID.randomUUID().toString());
				userState.setAuthenticated(false);
				CurrentUser.setUserState(userState);
				sessionService.forceSession();
			}
		}
	}
}
