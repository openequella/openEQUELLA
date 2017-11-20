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

package com.tle.web.loggedinusers;

import java.util.Date;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.services.user.UserSessionTimestamp;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@Bind
@SuppressWarnings("nls")
public class LoggedInUsersListSection extends AbstractPrototypeSection<LoggedInUsersListSection.LoggedInUsersListModel>
	implements
		HtmlRenderer
{
	@PlugKey("liu.title")
	private static Label LABEL_TITLE;
	@PlugKey("sessions.count")
	private static String KEY_SESSIONS;
	@PlugKey("webusers")
	private static Label LABEL_USERSNAME;
	@PlugKey("address")
	private static Label LABEL_IP;
	@PlugKey("loggedin")
	private static Label LABEL_LOGGED_IN_TIME;
	@PlugKey("lastaccess")
	private static Label LABEL_LAST_ACCESS_TIME;

	@ViewFactory
	private FreemarkerFactory freemarkerFactory;

	@Inject
	private UserSessionService userSessionService;
	@Inject
	private LoggedInUsersPrivilegeTreeProvider securityProvider;
	@Inject
	private DateRendererFactory dateRendererFactory;

	@Component(name = "s")
	private Table sessionsTable;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations.getDecorations(context).setTitle(LABEL_TITLE);
		Breadcrumbs.get(context).add(SettingsUtils.getBreadcrumb());

		final TableState sessionsTableState = sessionsTable.getState(context);
		int users = 0;
		for( UserSessionTimestamp session : userSessionService.getInstitutionSessions() )
		{
			final String username = session.getUsername();
			final Date created = session.getCreated();
			final Date accessed = session.getAccessed();
			final TableRow row = sessionsTableState.addRow(username, session.getHostAddress(),
				dateRendererFactory.createDateRenderer(created), dateRendererFactory.createDateRenderer(accessed));
			row.setSortData(username, null, created, accessed);
			users++;
		}

		final LoggedInUsersListModel model = getModel(context);
		model.setSessionsLabel(new PluralKeyLabel(KEY_SESSIONS, users));

		return freemarkerFactory.createResult("loggedinuserlist.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		sessionsTable.setColumnHeadings(LABEL_USERSNAME, LABEL_IP, LABEL_LOGGED_IN_TIME, LABEL_LAST_ACCESS_TIME);
		sessionsTable.setColumnSorts(Sort.SORTABLE_ASC, Sort.NONE, Sort.SORTABLE_ASC, Sort.PRIMARY_ASC);
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void checkAuthorised(SectionInfo info)
	{
		securityProvider.checkAuthorised();
	}

	@Override
	public Class<LoggedInUsersListModel> getModelClass()
	{
		return LoggedInUsersListModel.class;
	}

	public Table getSessionsTable()
	{
		return sessionsTable;
	}

	public static class LoggedInUsersListModel
	{
		private Label sessionsLabel;

		public Label getSessionsLabel()
		{
			return sessionsLabel;
		}

		public void setSessionsLabel(Label sessionsLabel)
		{
			this.sessionsLabel = sessionsLabel;
		}
	}
}
