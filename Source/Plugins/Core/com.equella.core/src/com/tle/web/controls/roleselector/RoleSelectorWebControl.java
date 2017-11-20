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

package com.tle.web.controls.roleselector;

import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.Format;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.wizard.controls.roleselector.RoleSelectorControl;
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
import com.tle.web.sections.equella.utils.SelectRoleDialog;
import com.tle.web.sections.equella.utils.SelectRoleDialog.CurrentRolesCallback;
import com.tle.web.sections.equella.utils.SelectedRole;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.controls.CCustomControl;
import com.tle.web.wizard.controls.WebControlModel;

@SuppressWarnings("nls")
@Bind
public class RoleSelectorWebControl extends AbstractWebControl<RoleSelectorWebControl.RoleSelectorWebControlModel>
	implements
		CurrentRolesCallback
{
	@PlugKey("rolesel.prompt.selectsingle")
	private static Label LABEL_PROMPTSINGLE;
	@PlugKey("rolesel.prompt.selectmultiple")
	private static Label LABEL_PROMPTMULTIPLE;
	@PlugKey("rolesel.title.selectsingle")
	private static Label LABEL_TITLESINGLE;
	@PlugKey("rolesel.title.selectmultiple")
	private static Label LABEL_TITLEMULTIPLE;

	@PlugKey("rolesel.confirmremove")
	private static Confirm KEY_CONFIRM;
	@PlugKey("rolesel.remove")
	private static Label LABEL_REMOVE;

	private RoleSelectorControl definitionControl;
	private CCustomControl storageControl;

	@Inject
	private ComponentFactory componentFactory;
	@Inject
	private SelectRoleDialog selectRoleDialog;
	@Inject
	private UserService userService;

	@ViewFactory(name="wizardFreemarkerFactory")
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component
	private SelectionsTable rolesTable;
	@Component
	@PlugKey("rolesel.button.selectrole")
	private Link addLink;

	private JSCallable removeRoleFunction;

	@Override
	public void setWrappedControl(final HTMLControl control)
	{
		definitionControl = new RoleSelectorControl((CustomControl) control.getControlBean());
		storageControl = (CCustomControl) control;
		super.setWrappedControl(control);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		addDisabler(context, addLink);
		return viewFactory.createResult("roleselectorwebcontrol.ftl", context);
	}

	@Override
	public void registered(final String id, final SectionTree tree)
	{
		super.registered(id, tree);

		final boolean multiple = definitionControl.isSelectMultiple();

		selectRoleDialog.setMultipleRoles(multiple);
		selectRoleDialog.setTitle(multiple ? LABEL_TITLEMULTIPLE : LABEL_TITLESINGLE);
		selectRoleDialog.setPrompt(multiple ? LABEL_PROMPTMULTIPLE : LABEL_PROMPTSINGLE);
		selectRoleDialog.setOkCallback(getReloadFunction(true, events.getEventHandler("select")));
		selectRoleDialog.setRolesCallback(this);

		componentFactory.registerComponent(id, "s", tree, selectRoleDialog);
		removeRoleFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("removeRole"),
			id + "roleselector");

		addLink.setClickHandler(selectRoleDialog.getOpenFunction());
		addLink.setDisablable(true);

		rolesTable.setSelectionsModel(new RolesModel());
		rolesTable.setAddAction(addLink);
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
	public void select(final SectionContext context, final String rolesJson) throws Exception
	{
		final RoleSelectorWebControlModel model = getModel(context);
		model.setSelectedRoles(SelectRoleDialog.rolesFromJsonString(rolesJson));
	}

	@EventHandlerMethod
	public void removeRole(final SectionContext context, final String roleId) throws Exception
	{
		storageControl.getValues().remove(roleId);
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		final RoleSelectorWebControlModel model = getModel(info);

		if( model.getSelectedRoles() != null )
		{
			final List<String> controlValues = storageControl.getValues();
			controlValues.clear();
			controlValues.addAll(Lists.transform(model.getSelectedRoles(), new Function<SelectedRole, String>()
			{
				@Override
				public String apply(SelectedRole role)
				{
					return role.getUuid();
				}
			}));
		}
	}

	@Override
	public Class<RoleSelectorWebControlModel> getModelClass()
	{
		return RoleSelectorWebControlModel.class;
	}

	@Override
	public List<SelectedRole> getCurrentSelectedRoles(final SectionInfo info)
	{
		return Lists.transform(storageControl.getValues(), new Function<String, SelectedRole>()
		{
			@Override
			public SelectedRole apply(String id)
			{
				final RoleBean roleBean = userService.getInformationForRole(id);
				final String displayName;
				if( roleBean == null )
				{
					displayName = id;
				}
				else
				{
					displayName = Format.format(roleBean);
				}
				return new SelectedRole(id, displayName);
			}
		});
	}

	public SelectRoleDialog getSelectRoleDialog()
	{
		return selectRoleDialog;
	}

	public SelectionsTable getRolesTable()
	{
		return rolesTable;
	}

	private class RolesModel extends DynamicSelectionsTableModel<String>
	{
		@Override
		protected List<String> getSourceList(SectionInfo info)
		{
			return storageControl.getValues();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, String roleId,
			List<SectionRenderable> actions, int index)
		{
			selection
				.setViewAction(new LabelRenderer(new TextLabel(userService.getInformationForRole(roleId).getName())));
			if( isEnabled() )
			{
				actions.add(makeRemoveAction(LABEL_REMOVE,
					new OverrideHandler(removeRoleFunction, roleId).addValidator(KEY_CONFIRM)));
			}
		}
	}

	public static class RoleSelectorWebControlModel extends WebControlModel
	{
		/**
		 * For shoving de-JSONed results into.
		 */
		private List<SelectedRole> selectedRoles;

		public List<SelectedRole> getSelectedRoles()
		{
			return selectedRoles;
		}

		public void setSelectedRoles(final List<SelectedRole> selectedRoles)
		{
			this.selectedRoles = selectedRoles;
		}
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return null;
	}
}