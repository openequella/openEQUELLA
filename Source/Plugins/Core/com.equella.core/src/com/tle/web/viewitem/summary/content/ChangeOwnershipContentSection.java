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

package com.tle.web.viewitem.summary.content;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.tle.beans.item.Item;
import com.tle.common.Format;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.services.user.UserService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.component.model.SelectionsTableState;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.utils.SelectUserDialog;
import com.tle.web.sections.equella.utils.SelectUserDialog.CurrentUsersCallback;
import com.tle.web.sections.equella.utils.SelectedUser;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewurl.ItemSectionInfo;

@SuppressWarnings("nls")
public class ChangeOwnershipContentSection
	extends
		AbstractContentSection<ChangeOwnershipContentSection.ChangeOwnershipModel>
	implements
		ViewableChildInterface,
		CurrentUsersCallback
{
	public static final String REQUIRED_PRIVILEGE = "REASSIGN_OWNERSHIP_ITEM";

	@PlugKey("summary.content.changeownership.pagetitle")
	private static Label TITLE_LABEL;

	@PlugKey("summary.content.changeownership.owner.button.reassign")
	private static Label OWNER_CHANGE;
	@PlugKey("summary.content.changeownership.owner.dialog.ok")
	private static Label OWNER_DIALOG_OK;
	@PlugKey("summary.content.changeownership.owner.dialog.title")
	private static Label OWNER_DIALOG_TITLE;
	@PlugKey("summary.content.changeownership.owner.dialog.prompt")
	private static Label OWNER_DIALOG_PROMPT;

	@PlugKey("summary.content.changeownership.share.nocollabs")
	private static Label COLLAB_NONE;
	@PlugKey("currentlyselectedstuff.remove")
	private static Label COLLAB_REMOVE;
	@PlugKey("share.confirmremove")
	private static Confirm COLLAB_REMOVE_CONFIRM;
	@PlugKey("summary.content.changeownership.share.dialog.ok")
	private static Label COLLAB_DIALOG_OK;
	@PlugKey("summary.content.changeownership.share.dialog.title")
	private static Label COLLAB_DIALOG_TITLE;
	@PlugKey("summary.content.changeownership.share.dialog.prompt")
	private static Label COLLAB_DIALOG_PROMPT;

	@PlugKey("summary.sidebar.itemdetailsgroup.owner")
	private static Label LABEL_OWNER;
	@PlugKey("summary.sidebar.itemdetailsgroup.collaborators")
	private static Label LABEL_COLLAB;

	@PlugKey("summary.content.changeownership.owner.receipt")
	private static Label OWNER_CHANGE_RECEIPT;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	private UserService userService;
	@Inject
	private ItemOperationFactory workflowFactory;
	@Inject
	private SelectUserDialog ownerSelect;
	@Inject
	private SelectUserDialog collabSelect;
	@Inject
	private UserLinkService userLinkService;
	@Inject
	private ReceiptService receiptService;

	private JSCallable removeUserFunc;
	private UserLinkSection userLinkSection;

	@Component(name = "ot")
	private SelectionsTable ownerTable;
	@Component(name = "ct")
	private SelectionsTable collabTable;

	@Component
	@PlugKey("summary.content.changeownership.share.button.add")
	private Link addCollab;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		userLinkSection = userLinkService.register(tree, id);

		// Owner
		ownerSelect.setTitle(OWNER_DIALOG_TITLE);
		ownerSelect.setPrompt(OWNER_DIALOG_PROMPT);
		ownerSelect.setOkCallback(events.getSubmitValuesFunction("changeOwner"));
		ownerSelect.setOkLabel(OWNER_DIALOG_OK);

		// Collaborators
		collabSelect.setTitle(COLLAB_DIALOG_TITLE);
		collabSelect.setPrompt(COLLAB_DIALOG_PROMPT);
		collabSelect.setMultipleUsers(true);
		collabSelect.setOkCallback(ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("addCollaborators"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "collaborators", "adjacentuls"));
		collabSelect.setOkLabel(COLLAB_DIALOG_OK);
		collabSelect.setUsersCallback(this);

		removeUserFunc = events.getSubmitValuesFunction("removeCollaborator");

		// Register selectors
		tree.registerInnerSection(ownerSelect, id);
		tree.registerInnerSection(collabSelect, id);

		addCollab.setClickHandler(collabSelect.getOpenFunction());

		ownerTable.setColumnHeadings(LABEL_OWNER, null);
		ownerTable.setColumnSorts(Sort.NONE, Sort.NONE);
		ownerTable.setSelectionsModel(new OwnerTableModel());

		collabTable.setColumnHeadings(LABEL_COLLAB, null);
		collabTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.NONE);
		collabTable.setSelectionsModel(new CollabTableModel());
		collabTable.setNothingSelectedText(COLLAB_NONE);
		collabTable.setAddAction(addCollab);
	}

	@Override
	public SectionResult renderHtml(final RenderEventContext context)
	{
		if( !canView(context) )
		{
			throw new AccessDeniedException("");
		}
		final ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(context);
		addDefaultBreadcrumbs(context, iinfo, TITLE_LABEL);
		displayBackButton(context);

		return viewFactory.createResult("viewitem/summary/content/changeownership.ftl", context);
	}

	@EventHandlerMethod
	public void changeOwner(final SectionInfo info, final String usersJson)
	{
		final ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(info);
		final SelectedUser user = SelectUserDialog.userFromJsonString(usersJson);

		if( user != null )
		{
			iinfo.modify(workflowFactory.changeOwner(user.getUuid()));
			receiptService.setReceipt(OWNER_CHANGE_RECEIPT);
			checkAccess(info);
		}
	}

	private void checkAccess(final SectionInfo info)
	{
		if( !canView(info) )
		{
			itemSummaryContentSection.setSummaryId(info, null);
		}
	}

	@EventHandlerMethod
	public void addCollaborators(SectionInfo info, String usersJson)
	{
		saveCollaborators(info,
			Collections2.transform(SelectUserDialog.usersFromJsonString(usersJson), new Function<SelectedUser, String>()
			{
				@Override
				public String apply(SelectedUser user)
				{
					return user.getUuid();
				}
			}));
	}

	@EventHandlerMethod
	public void removeCollaborator(SectionInfo info, String userId)
	{
		final ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(info);
		iinfo.modify(workflowFactory.modifyCollaborators(userId, true));
		checkAccess(info);
	}

	private void saveCollaborators(final SectionInfo info, final Collection<String> userIds)
	{
		ParentViewItemSectionUtils.getItemInfo(info)
			.modify(workflowFactory.addCollaborators(new HashSet<String>(userIds)));
	}

	@Override
	public List<SelectedUser> getCurrentSelectedUsers(SectionInfo info)
	{
		return Lists.newArrayList(
			Collections2.transform(ParentViewItemSectionUtils.getItemInfo(info).getItem().getCollaborators(),
				new Function<String, SelectedUser>()
				{
					@Override
					public SelectedUser apply(final String uuidOrEmail)
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
				}));
	}

	private Set<String> getList(Item item)
	{
		return item.getCollaborators();
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(info);
		return !itemInfo.getWorkflowStatus().isLocked() && itemInfo.hasPrivilege(REQUIRED_PRIVILEGE);
	}

	public SelectUserDialog getOwnerSelect()
	{
		return ownerSelect;
	}

	public SelectUserDialog getCollabSelect()
	{
		return collabSelect;
	}

	public SelectionsTable getOwnerTable()
	{
		return ownerTable;
	}

	public SelectionsTable getCollabTable()
	{
		return collabTable;
	}

	@Override
	public Class<ChangeOwnershipModel> getModelClass()
	{
		return ChangeOwnershipModel.class;
	}

	private class OwnerTableModel extends DynamicSelectionsTableModel<String>
	{
		@Override
		protected List<String> getSourceList(SectionInfo info)
		{
			final ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(info);
			final Item item = iinfo.getItem();
			return Collections.singletonList(item.getOwner());
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, String userId,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(new LinkRenderer(userLinkSection.createLink(info, userId)));
			actions.add(makeAction(OWNER_CHANGE, new OverrideHandler(ownerSelect.getOpenFunction())));
		}
	}

	private class CollabTableModel extends DynamicSelectionsTableModel<String>
	{
		@Override
		protected List<String> getSourceList(SectionInfo info)
		{
			final ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(info);
			final Item item = iinfo.getItem();
			return Lists.newArrayList(getList(item));
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, String userId,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(new LinkRenderer(userLinkSection.createLink(info, userId)));
			actions.add(makeRemoveAction(COLLAB_REMOVE,
				new OverrideHandler(removeUserFunc, userId).addValidator(COLLAB_REMOVE_CONFIRM)));
		}
	}

	public static class ChangeOwnershipModel
	{
		private SelectionsTableState owner;
		private SelectionsTableState collaborators;

		public SelectionsTableState getOwner()
		{
			return owner;
		}

		public void setOwner(SelectionsTableState owner)
		{
			this.owner = owner;
		}

		public SelectionsTableState getCollaborators()
		{
			return collaborators;
		}

		public void setCollaborators(SelectionsTableState collaborators)
		{
			this.collaborators = collaborators;
		}
	}
}
