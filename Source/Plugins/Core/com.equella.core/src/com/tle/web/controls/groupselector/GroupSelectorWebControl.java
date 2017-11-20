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

package com.tle.web.controls.groupselector;

import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.Format;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.wizard.controls.groupselector.GroupSelectorControl;
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
import com.tle.web.sections.equella.utils.SelectGroupDialog;
import com.tle.web.sections.equella.utils.SelectGroupDialog.CurrentGroupsCallback;
import com.tle.web.sections.equella.utils.SelectedGroup;
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
public class GroupSelectorWebControl extends AbstractWebControl<GroupSelectorWebControl.GroupSelectorWebControlModel>
	implements
		CurrentGroupsCallback
{
	@PlugKey("groupsel.prompt.selectsingle")
	private static Label LABEL_PROMPTSINGLE;
	@PlugKey("groupsel.prompt.selectmultiple")
	private static Label LABEL_PROMPTMULTIPLE;
	@PlugKey("groupsel.title.selectsingle")
	private static Label LABEL_TITLESINGLE;
	@PlugKey("groupsel.title.selectmultiple")
	private static Label LABEL_TITLEMULTIPLE;

	@PlugKey("groupsel.confirmremove")
	private static Confirm KEY_CONFIRM;
	@PlugKey("groupsel.remove")
	private static Label LABEL_REMOVE;

	private GroupSelectorControl definitionControl;
	private CCustomControl storageControl;

	@Inject
	private ComponentFactory componentFactory;
	@Inject
	private SelectGroupDialog selectGroupDialog;
	@Inject
	private UserService userService;

	@ViewFactory(name="wizardFreemarkerFactory")
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component
	private SelectionsTable groupsTable;
	@Component
	@PlugKey("groupsel.button.selectgroup")
	private Link addLink;

	private JSCallable removeGroupFunction;

	@Override
	public void setWrappedControl(final HTMLControl control)
	{
		definitionControl = new GroupSelectorControl((CustomControl) control.getControlBean());
		storageControl = (CCustomControl) control;
		super.setWrappedControl(control);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		addDisabler(context, addLink);
		return viewFactory.createResult("groupselectorwebcontrol.ftl", context);
	}

	@Override
	public void registered(final String id, final SectionTree tree)
	{
		super.registered(id, tree);

		final boolean multiple = definitionControl.isSelectMultiple();

		selectGroupDialog.setMultipleGroups(multiple);
		selectGroupDialog.setTitle(multiple ? LABEL_TITLEMULTIPLE : LABEL_TITLESINGLE);
		selectGroupDialog.setPrompt(multiple ? LABEL_PROMPTMULTIPLE : LABEL_PROMPTSINGLE);
		selectGroupDialog.setOkCallback(getReloadFunction(true, events.getEventHandler("select")));
		selectGroupDialog.setGroupsCallback(this);
		selectGroupDialog.setGroupFilter(definitionControl.isRestricted(GroupSelectorControl.KEY_RESTRICT_GROUPS)
			? definitionControl.getRestrictedTo(GroupSelectorControl.KEY_RESTRICT_GROUPS) : null);

		componentFactory.registerComponent(id, "s", tree, selectGroupDialog);
		removeGroupFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("removeGroup"),
			id + "groupselector");

		addLink.setClickHandler(selectGroupDialog.getOpenFunction());
		addLink.setDisablable(true);

		groupsTable.setSelectionsModel(new GroupsModel());
		groupsTable.setAddAction(addLink);
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
	public void select(final SectionContext context, final String groupsJson) throws Exception
	{
		final GroupSelectorWebControlModel model = getModel(context);
		model.setSelectedGroups(SelectGroupDialog.groupsFromJsonString(groupsJson));
	}

	@EventHandlerMethod
	public void removeGroup(final SectionContext context, final String groupId) throws Exception
	{
		storageControl.getValues().remove(groupId);
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		final GroupSelectorWebControlModel model = getModel(info);

		if( model.getSelectedGroups() != null )
		{
			final List<String> controlValues = storageControl.getValues();
			controlValues.clear();
			controlValues.addAll(Lists.transform(model.getSelectedGroups(), new Function<SelectedGroup, String>()
			{
				@Override
				public String apply(SelectedGroup group)
				{
					return group.getUuid();
				}
			}));
		}
	}

	@Override
	public Class<GroupSelectorWebControlModel> getModelClass()
	{
		return GroupSelectorWebControlModel.class;
	}

	@Override
	public List<SelectedGroup> getCurrentSelectedGroups(final SectionInfo info)
	{
		return Lists.transform(storageControl.getValues(), new Function<String, SelectedGroup>()
		{
			@Override
			public SelectedGroup apply(String id)
			{
				final GroupBean groupBean = userService.getInformationForGroup(id);
				final String displayName;
				if( groupBean == null )
				{
					displayName = id;
				}
				else
				{
					displayName = Format.format(groupBean);
				}
				return new SelectedGroup(id, displayName);
			}
		});
	}

	public SelectGroupDialog getSelectGroupDialog()
	{
		return selectGroupDialog;
	}

	public SelectionsTable getGroupsTable()
	{
		return groupsTable;
	}

	private class GroupsModel extends DynamicSelectionsTableModel<String>
	{
		@Override
		protected List<String> getSourceList(SectionInfo info)
		{
			return storageControl.getValues();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, String groupId,
			List<SectionRenderable> actions, int index)
		{
			selection
				.setViewAction(new LabelRenderer(new TextLabel(userService.getInformationForGroup(groupId).getName())));
			if( isEnabled() )
			{
				actions.add(makeRemoveAction(LABEL_REMOVE,
					new OverrideHandler(removeGroupFunction, groupId).addValidator(KEY_CONFIRM)));
			}
		}
	}

	public static class GroupSelectorWebControlModel extends WebControlModel
	{
		/**
		 * For shoving de-JSONed results into.
		 */
		private List<SelectedGroup> selectedGroups;

		public List<SelectedGroup> getSelectedGroups()
		{
			return selectedGroups;
		}

		public void setSelectedGroups(final List<SelectedGroup> selectedGroups)
		{
			this.selectedGroups = selectedGroups;
		}
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return null;
	}
}