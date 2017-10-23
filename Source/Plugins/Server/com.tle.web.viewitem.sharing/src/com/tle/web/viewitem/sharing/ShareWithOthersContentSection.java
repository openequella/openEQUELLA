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

package com.tle.web.viewitem.sharing;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Format;
import com.tle.common.URLUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.usermanagement.standard.wrapper.SharePassWrapper;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
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
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.ItemNameLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewurl.ItemSectionInfo;

@SuppressWarnings("nls")
@Bind
public class ShareWithOthersContentSection extends AbstractShareWithOthersSection implements CurrentUsersCallback
{
	public static final String REQUIRED_PRIVILEGE = "SHARE_ITEM";

	@PlugKey("notify.confirmremove")
	private static Confirm CONFIRM_REMOVE;
	@PlugKey("notify.remove")
	private static Label REMOVE_LABEL;
	@PlugKey("notify.nousersselected")
	private static Label NO_USERS_SELECTED_LABEL;
	@PlugKey("summary.content.sharewithothers.share")
	private static String TIME;
	@PlugKey("summary.content.sharewithothers.users")
	private static Label LABEL_OTHERS;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	private SelectUserDialog userSelect;
	@Inject
	private ItemOperationFactory workflowFactory;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;

	@Component
	private SingleSelectionList<VoidKeyOption> daysList;

	@Component
	@PlugKey("summary.content.sharewithothers.notify.button")
	private Link selectUserToNotify;

	@Component(name = "ot")
	private SelectionsTable othersTable;

	private UpdateDomFunction removeUserFunction;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		userLinkSection = userLinkService.register(tree, id);

		daysList.setListModel(new SimpleHtmlListModel<VoidKeyOption>(new VoidKeyOption(t("1day"), "1"),
			new VoidKeyOption(t("2days"), "2"), new VoidKeyOption(t("3days"), "3"), new VoidKeyOption(t("4days"), "4"),
			new VoidKeyOption(t("5days"), "5"), new VoidKeyOption(t("6days"), "6"), new VoidKeyOption(t("1week"), "7"),
			new VoidKeyOption(t("2weeks"), "14"), new VoidKeyOption(t("3weeks"), "21"),
			new VoidKeyOption(t("4weeks"), "28"), new VoidKeyOption(t("2months"), "60"),
			new VoidKeyOption(t("3months"), "91"), new VoidKeyOption(t("4months"), "122"),
			new VoidKeyOption(t("5months"), "152"), new VoidKeyOption(t("6months"), "182")));

		JSCallable inplace = ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE);

		userSelect.setMultipleUsers(true);
		userSelect.setOkCallback(ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("addNotifications"),
			inplace, "selectedusers"));
		userSelect.setAjax(true);
		userSelect.setOkLabel(OK_LABEL);
		userSelect.setUsersCallback(this);
		tree.registerInnerSection(userSelect, id);

		selectUserToNotify.setClickHandler(userSelect.getOpenFunction());
		removeUserFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("removeNotification"),
			inplace, "selectedusers");

		othersTable.setColumnHeadings(LABEL_OTHERS, null);
		othersTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.NONE);
		othersTable.setSelectionsModel(new OthersTableModel());
		othersTable.setNothingSelectedText(NO_USERS_SELECTED_LABEL);
		othersTable.setAddAction(selectUserToNotify);
	}

	private static String t(String keyPart)
	{
		return MessageFormat.format("{0}.{1}", TIME, keyPart);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView(context) )
		{
			throw new AccessDeniedException("");
		}

		ShareModel model = getModel(context);

		ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(context);

		model.setSharePassOn(sharePassService.isEnabled());
		model.setShowNotifyWhenLive(iinfo.getWorkflowStatus().getStatusName() != ItemStatus.LIVE);

		addDefaultBreadcrumbs(context, iinfo, TITLE_LABEL);

		return viewFactory.createResult("sharewithothers.ftl", context);
	}

	@Override
	public List<SelectedUser> getCurrentSelectedUsers(SectionInfo info)
	{
		return Lists.newArrayList(
			Collections2.transform(ParentViewItemSectionUtils.getItemInfo(info).getItem().getNotifications(),
				new Function<String, SelectedUser>()
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
				}));
	}

	@EventHandlerMethod
	public void removeNotification(SectionInfo info, String userId)
	{
		final ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(info);
		final Set<String> users = new HashSet<String>(iinfo.getItem().getNotifications());
		users.remove(userId);
		saveNotifications(info, users);
	}

	@EventHandlerMethod
	public void addNotifications(SectionInfo info, String usersJson)
	{
		saveNotifications(info,
			Lists.transform(SelectUserDialog.usersFromJsonString(usersJson), new Function<SelectedUser, String>()
			{
				@Override
				public String apply(SelectedUser user)
				{
					return user.getUuid();
				}
			}));
	}

	private void saveNotifications(final SectionInfo info, final Collection<String> userIds)
	{
		ParentViewItemSectionUtils.getItemInfo(info)
			.modify(workflowFactory.modifyNotifications(new HashSet<String>(userIds)));
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(info);
		return iinfo.hasPrivilege(REQUIRED_PRIVILEGE)
			&& (sharePassService.isEnabled() || iinfo.getWorkflowStatus().getStatusName() != ItemStatus.LIVE);
	}

	protected void saveNewUsers(final SectionInfo info, final Collection<String> userIds)
	{
		final ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(info);

		iinfo.modify(workflowFactory.modifyNotifications(new HashSet<String>(userIds)));
	}

	private class OthersTableModel extends DynamicSelectionsTableModel<String>
	{
		@Override
		protected Collection<String> getSourceList(SectionInfo info)
		{
			return ParentViewItemSectionUtils.getItemInfo(info).getItem().getNotifications();
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, String userId,
			List<SectionRenderable> actions, int index)
		{
			selection.setViewAction(new LinkRenderer(userLinkSection.createLink(info, userId)));
			actions.add(makeRemoveAction(REMOVE_LABEL,
				new OverrideHandler(removeUserFunction, userId).addValidator(CONFIRM_REMOVE)));
		}
	}

	public SingleSelectionList<VoidKeyOption> getDaysList()
	{
		return daysList;
	}

	public SelectUserDialog getUserSelect()
	{
		return userSelect;
	}

	public SelectionsTable getOthersTable()
	{
		return othersTable;
	}

	@Override
	protected String createEmail(SectionInfo info)
	{
		return buildEmail(info);
	}

	private String buildEmail(SectionInfo info)
	{
		StringBuilder email = new StringBuilder();

		final ItemSectionInfo iinfo = ParentViewItemSectionUtils.getItemInfo(info);
		final int days = Integer.parseInt(daysList.getSelectedValueAsString(info));
		final Date date = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days));
		final String uuid = sharePassService.add(iinfo.getItem(), emailField.getValue(info), date);
		final String url = URLUtils.newURL(institutionService.getInstitutionUrl(),
			(iinfo.getItemdir() + "?token=" + SharePassWrapper.TOKEN_PREFIX + uuid)).toString();

		email.append(s("intro", getUser(CurrentUser.getDetails())));

		email.append(messageField.getValue(info));
		email.append("\n\n");
		email.append(s("item.name", new ItemNameLabel(iinfo.getItem(), bundleCache)));
		email.append(s("item.link", url));
		email.append(s("item.version", iinfo.getItem().getVersion()));
		email.append(s("item.owner", getUser(userService.getInformationForUser(iinfo.getItem().getOwner()))));
		email.append("\n");
		email.append(s("outro"));

		return email.toString();
	}

	@Override
	public void clearForm(SectionInfo info)
	{
		super.clearForm(info);
		daysList.setSelectedStringValue(info, null);
	}
}
