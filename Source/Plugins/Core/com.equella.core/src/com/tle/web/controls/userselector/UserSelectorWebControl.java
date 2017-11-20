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

package com.tle.web.controls.userselector;

import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.Format;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.wizard.controls.userselector.UserSelectorControl;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.utils.SelectUserDialog;
import com.tle.web.sections.equella.utils.SelectUserDialog.CurrentUsersCallback;
import com.tle.web.sections.equella.utils.SelectedUser;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.controls.CCustomControl;
import com.tle.web.wizard.controls.WebControlModel;

@SuppressWarnings("nls")
@Bind
public class UserSelectorWebControl extends AbstractWebControl<UserSelectorWebControl.UserSelectorWebControlModel>
	implements
		CurrentUsersCallback
{
	@PlugKey("usersel.prompt.selectsingle")
	private static Label LABEL_PROMPTSINGLE;
	@PlugKey("usersel.prompt.selectmultiple")
	private static Label LABEL_PROMPTMULTIPLE;
	@PlugKey("usersel.title.selectsingle")
	private static Label LABEL_TITLESINGLE;
	@PlugKey("usersel.title.selectmultiple")
	private static Label LABEL_TITLEMULTIPLE;

	@PlugKey("usersel.confirmremove")
	private static Confirm KEY_CONFIRM;
	@PlugKey("usersel.remove")
	private static Label LABEL_REMOVE;

	private UserSelectorControl definitionControl;
	private CCustomControl storageControl;

	@Inject
	private ComponentFactory componentFactory;
	@Inject
	private SelectUserDialog selectUserDialog;
	@Inject
	private UserService userService;
	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;

	@ViewFactory(name="wizardFreemarkerFactory")
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component
	private SelectionsTable usersTable;
	@Component
	@PlugKey("usersel.button.selectuser")
	private Link addLink;

	private JSCallable removeUserFunction;

	@Override
	public void setWrappedControl(final HTMLControl control)
	{
		definitionControl = new UserSelectorControl((CustomControl) control.getControlBean());
		storageControl = (CCustomControl) control;
		super.setWrappedControl(control);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		addDisabler(context, addLink);
		return viewFactory.createResult("userselectorwebcontrol.ftl", context);
	}

	@Override
	public void registered(final String id, final SectionTree tree)
	{
		super.registered(id, tree);

		userLinkSection = userLinkService.register(tree, id);

		final boolean multiple = definitionControl.isSelectMultiple();

		selectUserDialog.setMultipleUsers(multiple);
		selectUserDialog.setTitle(multiple ? LABEL_TITLEMULTIPLE : LABEL_TITLESINGLE);
		selectUserDialog.setPrompt(multiple ? LABEL_PROMPTMULTIPLE : LABEL_PROMPTSINGLE);
		selectUserDialog.setOkCallback(getReloadFunction(true, events.getEventHandler("select")));
		selectUserDialog.setUsersCallback(this);
		selectUserDialog.setGroupFilter(definitionControl.isRestricted(UserSelectorControl.KEY_RESTRICT_USER_GROUPS)
			? definitionControl.getRestrictedTo(UserSelectorControl.KEY_RESTRICT_USER_GROUPS) : null);

		componentFactory.registerComponent(id, "s", tree, selectUserDialog);
		removeUserFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("removeUser"),
			id + "userselector");

		addLink.setClickHandler(selectUserDialog.getOpenFunction());
		addLink.setDisablable(true);

		usersTable.setSelectionsModel(new UsersModel());
		usersTable.setAddAction(addLink);
	}

	@Override
	public boolean isEmpty()
	{
		return storageControl.getValues().size() == 0;
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		if( storageControl.hasTargets() )
		{
			return storageControl.getDefaultPowerSearchQuery(storageControl.getValues(), false);
		}
		return null;
	}

	@EventHandlerMethod
	public void select(final SectionContext context, final String usersJson) throws Exception
	{
		final UserSelectorWebControlModel model = getModel(context);
		model.setSelectedUsers(SelectUserDialog.usersFromJsonString(usersJson));
	}

	@EventHandlerMethod
	public void removeUser(final SectionContext context, final String userId) throws Exception
	{
		storageControl.getValues().remove(userId);
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		final UserSelectorWebControlModel model = getModel(info);

		if( model.getSelectedUsers() != null )
		{
			final List<String> controlValues = storageControl.getValues();
			controlValues.clear();
			controlValues.addAll(Lists.transform(model.getSelectedUsers(), new Function<SelectedUser, String>()
			{
				@Override
				public String apply(SelectedUser user)
				{
					return user.getUuid();
				}
			}));
		}
	}

	@Override
	public Class<UserSelectorWebControlModel> getModelClass()
	{
		return UserSelectorWebControlModel.class;
	}

	@Override
	public List<SelectedUser> getCurrentSelectedUsers(final SectionInfo info)
	{
		return Lists.transform(storageControl.getValues(), new Function<String, SelectedUser>()
		{
			@Override
			public SelectedUser apply(String uuidOrEmail)
			{
				final UserBean userBean = userService.getInformationForUser(uuidOrEmail);
				final String displayName;
				if( userBean == null )
				{
					displayName = uuidOrEmail;
				}
				else
				{
					displayName = Format.format(userBean);
				}
				return new SelectedUser(uuidOrEmail, displayName);
			}
		});
	}

	public SelectUserDialog getSelectUserDialog()
	{
		return selectUserDialog;
	}

	public SelectionsTable getUsersTable()
	{
		return usersTable;
	}

	private class UsersModel extends DynamicSelectionsTableModel<String>
	{
		@Override
		protected List<String> getSourceList(SectionInfo info)
		{
			return storageControl.getValues();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, String userId,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(new LinkRenderer(userLinkSection.createLink(info, userId)));
			if( isEnabled() )
			{
				actions.add(makeRemoveAction(LABEL_REMOVE,
					new OverrideHandler(removeUserFunction, userId).addValidator(KEY_CONFIRM)));
			}
		}
	}

	public static class UserSelectorWebControlModel extends WebControlModel
	{
		/**
		 * For shoving de-JSONed results into.
		 */
		private List<SelectedUser> selectedUsers;

		public List<SelectedUser> getSelectedUsers()
		{
			return selectedUsers;
		}

		public void setSelectedUsers(final List<SelectedUser> selectedUsers)
		{
			this.selectedUsers = selectedUsers;
		}
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return null;
	}
}
