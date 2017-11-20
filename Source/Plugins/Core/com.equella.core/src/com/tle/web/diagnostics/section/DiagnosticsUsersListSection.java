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

package com.tle.web.diagnostics.section;

import java.util.List;

import javax.inject.Inject;

import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.utils.SelectGroupDialog;
import com.tle.web.sections.equella.utils.SelectUserDialog;
import com.tle.web.sections.equella.utils.SelectedGroup;
import com.tle.web.sections.equella.utils.SelectedUser;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@Bind
@SuppressWarnings("nls")
public class DiagnosticsUsersListSection extends AbstractPrototypeSection<DiagnosticsUsersListSection.Model>
	implements
		HtmlRenderer
{
	@PlugKey("diagnostics.title")
	private static Label LABEL_TITLE;

	@PlugKey("no.group.found")
	private static Label LABEL_NO_GROUP_FOUND;
	@PlugKey("no.user.found")
	private static Label LABEL_NO_USER_FOUND;

	@PlugKey("table.gropus.prefix")
	private static Label GROUP_PREFIX;

	@PlugKey("table.users.prefix")
	private static Label USER_PREFIX;

	@ViewFactory
	private FreemarkerFactory freemarkerFactory;
	@Component(name = "g")
	private Button groupsDialog;
	@Component(name = "u")
	private Button usersDialog;

	@Inject
	private UserService userService;
	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;

	@Inject
	@Component
	private SelectUserDialog userGroupsDialog;

	@Inject
	@Component
	private SelectGroupDialog selectGroupDialog;

	@Component
	private SelectionsTable groupsTable;

	@Component
	private Table usersTable;

	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations.getDecorations(context).setTitle(LABEL_TITLE);
		Breadcrumbs.get(context).add(SettingsUtils.getBreadcrumb());

		return freemarkerFactory.createResult("diagnostics.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		userLinkSection = userLinkService.register(tree, id);
		groupsTable.setFilterable(true);
		usersTable.setFilterable(true);

		getUserGroupsDialog().setAjax(true);
		getUserGroupsDialog().setMultipleUsers(false);

		JSCallable inplace = ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE);
		userGroupsDialog.setOkCallback(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("getGroups"), inplace, "usergroups"));

		getSelectGroupDialog().setAjax(true);
		getSelectGroupDialog().setMultipleGroups(false);
		getSelectGroupDialog().setOkCallback(
			ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("getMembers"), inplace, "groupmembers"));
	}

	@EventHandlerMethod
	public void getGroups(SectionInfo info, String usersJson) throws Exception
	{
		final SelectedUser selectedUser = SelectUserDialog.userFromJsonString(usersJson);
		final Model model = getModel(info);

		if( selectedUser != null )
		{
			String userId = selectedUser.getUuid();
			HtmlLinkState userLink = userLinkSection.createLink(info, userId);
			model.setUserSelected(true);
			groupsTable.setColumnHeadings(info, GROUP_PREFIX.getText() + " " + userLink.getLabel().getText());
			List<GroupBean> groups = userService.getGroupsContainingUser(userId);

			if( groups.size() > 0 )
			{
				for( GroupBean group : groups )
				{
					groupsTable.addRow(info, group.getName());
				}
			}
			else
			{
				groupsTable.addRow(info, LABEL_NO_GROUP_FOUND);
			}
		}
		else
		{
			model.setUserSelected(false);
		}
	}

	@EventHandlerMethod
	public void getMembers(SectionInfo info, String groupJson) throws Exception
	{
		final SelectedGroup selectedGroup = SelectGroupDialog.groupFromJsonString(groupJson);
		final Model model = getModel(info);
		if( selectedGroup != null )
		{
			model.setGroupSelected(true);
			String groupId = selectedGroup.getUuid();

			final List<UserBean> users = userService.getUsersInGroup(groupId, true);

			final TableState usersTableState = usersTable.getState(info);

			usersTableState.setColumnHeadings(USER_PREFIX.getText() + " " + selectedGroup.getDisplayName());

			if( users.size() > 0 )
			{
				for( UserBean user : users )
				{
					usersTableState.addRow(userLinkSection.createLink(info, user.getUniqueID()));
				}
			}
			else
			{
				usersTableState.addRow(LABEL_NO_USER_FOUND);
			}
		}
		else
		{
			model.setGroupSelected(false);
		}
	}

	public Button getGroupsDialog()
	{
		return groupsDialog;
	}

	public Button getUsersDialog()
	{
		return usersDialog;
	}

	@Override
	public Class<Model> getModelClass()
	{
		return Model.class;
	}

	/**
	 * @return the selectUserDialog
	 */
	public SelectUserDialog getUserGroupsDialog()
	{
		return userGroupsDialog;
	}

	/**
	 * @return the selectGroupDialog
	 */
	public SelectGroupDialog getSelectGroupDialog()
	{
		return selectGroupDialog;
	}

	/**
	 * @return the groupsTable
	 */
	public SelectionsTable getGroupsTable()
	{
		return groupsTable;
	}

	/**
	 * @return the usersTable
	 */
	public Table getUsersTable()
	{
		return usersTable;
	}

	public static class Model
	{
		private boolean userSelected;
		private boolean groupSelected;

		public boolean isUserSelected()
		{
			return userSelected;
		}

		public void setUserSelected(boolean userSelected)
		{
			this.userSelected = userSelected;
		}

		public boolean isGroupSelected()
		{
			return groupSelected;
		}

		public void setGroupSelected(boolean groupSelected)
		{
			this.groupSelected = groupSelected;
		}
	}
}
