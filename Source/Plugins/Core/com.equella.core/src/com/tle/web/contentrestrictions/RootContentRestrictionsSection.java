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

package com.tle.web.contentrestrictions;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.tle.common.FileSizeUtils;
import com.tle.common.quota.settings.QuotaSettings;
import com.tle.common.quota.settings.QuotaSettings.UserQuota;
import com.tle.common.recipientselector.formatter.ExpressionFormatter;
import com.tle.core.services.user.UserService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.contentrestrictions.dialog.AddBannedExtDialog;
import com.tle.web.contentrestrictions.dialog.SelectedQuota;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TemplateResultCollector;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author larry
 */
@SuppressWarnings("nls")
@TreeIndexed
public class RootContentRestrictionsSection extends OneColumnLayout<OneColumnLayoutModel>
{
	@PlugKey("contentrestrictions.title")
	private static Label TITLE_LABEL;

	@PlugKey("bannedext.empty")
	private static Label NO_BANNED_EXTENSIONS;
	@PlugKey("bannedext.remove")
	private static Label REMOVE_BANNED_EXTENSION;
	@PlugKey("bannedext.remove.confirm")
	private static Confirm CONFIRM_REMOVE_BANNED_EXTENSION;

	@PlugKey("quota.table.size")
	private static Label QUOTA_COLUMN_SIZE;
	@PlugKey("quota.table.expression")
	private static Label QUOTA_COLUMN_USERS;
	@PlugKey("quota.label.edit")
	private static Label LABEL_EDIT_QUOTA;
	@PlugKey("quota.label.delete")
	private static Label LABEL_DELETE_QUOTA;
	@PlugKey("quota.label.confirm.delete")
	private static Confirm LABEL_CONFIRM_DELETE_QUOTA;
	@PlugKey("quota.table.empty")
	private static Label LABEL_NO_QUOTAS;
	@PlugKey("quota.label.moveup")
	private static Label LABEL_MOVE_UP;
	@PlugKey("quota.label.movedown")
	private static Label LABEL_MOVE_DOWN;
	@PlugKey("quota.label.invalidexpression")
	private static Label LABEL_INVALID_EXPRESSION;
	@PlugURL("images/moveup.gif")
	private static String URL_ICON_UP;
	@PlugURL("images/movedown.gif")
	private static String URL_ICON_DOWN;

	@Inject
	private ContentRestrictionsPrivilegeTreeProvider securityProvider;
	@Inject
	private ConfigurationService configService;
	@Inject
	private UserService userService;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component
	private SelectionsTable bannedExtensions;
	@Component
	private SelectionsTable userQuotas;

	@Component(name = "ael")
	@PlugKey("addbannedext.button")
	private Link addBannedExtLink;
	@Component(name = "aed")
	@Inject
	private AddBannedExtDialog addBannedExtDialog;
	@Component(name = "aql")
	@PlugKey("quota.table.link.add")
	private Link addUserQuotaLink;

	@TreeLookup
	private EditContentRestrictionsSection editContentRestrictionsSection;

	@Override
	public void registered(String id, final SectionTree tree)
	{
		super.registered(id, tree);

		// Banned extensions
		addBannedExtDialog.setOkCallback(getAjaxUpdate(tree, "addBannedExtension", "bannedExtensions"));
		addBannedExtLink.setClickHandler(addBannedExtDialog.getOpenFunction());

		bannedExtensions.setAddAction(addBannedExtLink);
		bannedExtensions.setNothingSelectedText(NO_BANNED_EXTENSIONS);
		bannedExtensions.setSelectionsModel(new DynamicSelectionsTableModel<String>()
		{
			private final UpdateDomFunction deleteFunc = getAjaxUpdate(tree, "removeBannedExtension",
				"bannedExtensions");

			@Override
			protected List<String> getSourceList(SectionInfo info)
			{
				final List<String> extensions = Lists.newArrayList(getConfig().getBannedExtensions());
				Collections.sort(extensions);
				return extensions;
			}

			@Override
			protected void transform(SectionInfo info, SelectionsTableSelection selection, String ext,
				List<SectionRenderable> actions, int index)
			{
				selection.setName(new TextLabel(ext));
				actions.add(makeRemoveAction(REMOVE_BANNED_EXTENSION,
					new OverrideHandler(deleteFunc, ext).addValidator(CONFIRM_REMOVE_BANNED_EXTENSION)));
			}
		});

		final SubmitValuesFunction editFunc = events.getSubmitValuesFunction("editUserQuota");

		userQuotas.setColumnHeadings(QUOTA_COLUMN_SIZE, QUOTA_COLUMN_USERS, "");
		userQuotas.setNothingSelectedText(LABEL_NO_QUOTAS);
		userQuotas.setAddAction(addUserQuotaLink);
		userQuotas.setSelectionsModel(new DynamicSelectionsTableModel<UserQuota>()
		{
			private final UpdateDomFunction deleteFunc = getAjaxUpdate(tree, "removeUserQuota", "userQuotas");
			private final UpdateDomFunction upDownFunc = getAjaxUpdate(tree, "shiftUserQuota", "userQuotas");

			@Override
			protected List<UserQuota> getSourceList(SectionInfo info)
			{
				return getConfig().getQuotas();
			}

			@Override
			protected void transform(SectionInfo info, SelectionsTableSelection selection, UserQuota quota,
				List<SectionRenderable> actions, int index)
			{
				long quotaSize = quota.getSize();
				String quotaExpr = quota.getExpression();

				selection.setName(new TextLabel(FileSizeUtils.humanReadableFileSize(quotaSize)));

				Label expressionLabel;
				try
				{
					expressionLabel = new TextLabel(new ExpressionFormatter(userService).convertToInfix(quotaExpr));
				}
				catch( Exception e )
				{
					expressionLabel = LABEL_INVALID_EXPRESSION;
				}
				selection.addColumn(expressionLabel);

				actions.add(makeAction(LABEL_EDIT_QUOTA,
					new OverrideHandler(editFunc, index, quotaSize, Strings.nullToEmpty(quotaExpr))));

				actions.add(makeAction(LABEL_DELETE_QUOTA,
					new OverrideHandler(deleteFunc, index).addValidator(LABEL_CONFIRM_DELETE_QUOTA)));

				final HtmlLinkState upLinkState = new HtmlLinkState(new OverrideHandler(upDownFunc, index, true));

				upLinkState.setId("qup");
				final LinkRenderer upLink = new LinkRenderer(upLinkState);
				upLink.setNestedRenderable(new ImageRenderer(URL_ICON_UP, LABEL_MOVE_UP)).addClass("position");

				final HtmlLinkState downLinkState = new HtmlLinkState(new OverrideHandler(upDownFunc, index, false));
				downLinkState.setId("qdn");
				final LinkRenderer downLink = new LinkRenderer(downLinkState);
				downLink.setNestedRenderable(new ImageRenderer(URL_ICON_DOWN, LABEL_MOVE_DOWN)).addClass("position");

				// Up and down in same action column
				actions.add(CombinedRenderer.combineMultipleResults(upLink, downLink));
			}
		});

		addUserQuotaLink.setStyleClass("add");
		addUserQuotaLink.setClickHandler(new OverrideHandler(editFunc, -1, 0, ""));
	}

	private UpdateDomFunction getAjaxUpdate(SectionTree tree, String eventHandlerName, String... ajaxIds)
	{
		return ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler(eventHandlerName),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), ajaxIds);
	}

	@EventHandlerMethod
	public void editUserQuota(SectionInfo info, int index, long quotaSize, String expression)
	{
		SelectedQuota selected = new SelectedQuota(index, quotaSize, expression);
		editContentRestrictionsSection.editUserQuota(info, selected);
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		securityProvider.checkAuthorised();

		final OneColumnLayoutModel model = getModel(info);

		final TemplateResultCollector collector = new TemplateResultCollector();
		final SectionId modalSection = model.getModalSection();
		if( modalSection != null )
		{
			SectionUtils.renderSection(info, modalSection, collector);
			return collector.getTemplateResult();
		}
		// else
		return new GenericTemplateResult(view.createNamedResult(BODY, "contentrestrictions.ftl", this));
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		OneColumnLayoutModel model = getModel(info);
		SectionId modalSection = model.getModalSection();
		crumbs.add(SettingsUtils.getBreadcrumb());

		if( modalSection != null )
		{
			SectionId section = info.getSectionForId(modalSection);
			if( section instanceof ModalContentRestrictionsSection )
			{
				((ModalContentRestrictionsSection) section).addBreadcrumbsAndTitle(info, decorations, crumbs);
				return;
			}
		}
		decorations.setTitle(TITLE_LABEL);
	}

	private QuotaSettings getConfig()
	{
		return configService.getProperties(new QuotaSettings());
	}

	@EventHandlerMethod
	public void addBannedExtension(SectionInfo info, String ext)
	{
		final QuotaSettings config = getConfig();
		final List<String> extensions = config.getBannedExtensions();
		final String upperCase = ext.trim().toUpperCase();
		if( !extensions.contains(upperCase) )
		{
			extensions.add(upperCase);
			Collections.sort(extensions);
			configService.setProperties(config);
		}
	}

	@EventHandlerMethod
	public void removeBannedExtension(SectionInfo info, String key)
	{
		final QuotaSettings config = getConfig();
		config.getBannedExtensions().remove(key);
		configService.setProperties(config);
	}

	public void addUserAndQuota(SectionInfo info, SelectedQuota selected)
	{
		final QuotaSettings config = getConfig();
		final UserQuota uq;
		int quotaIndex = selected.getQuotaIndex();
		if( quotaIndex == -1 )
		{
			uq = new UserQuota();
			config.getQuotas().add(uq);
		}
		else
		{
			uq = config.getQuotas().get(quotaIndex);
		}
		uq.setExpression(selected.getExpression());
		uq.setSize(selected.getQuota());

		configService.setProperties(config);
	}

	@EventHandlerMethod
	public void removeUserQuota(SectionInfo info, int index)
	{
		final QuotaSettings config = getConfig();
		config.getQuotas().remove(index);
		configService.setProperties(config);
	}

	@EventHandlerMethod
	public void shiftUserQuota(SectionInfo info, int index, boolean up)
	{
		final QuotaSettings config = getConfig();
		final List<UserQuota> quotas = config.getQuotas();
		final UserQuota uq = quotas.get(index);

		int i = index;
		if( up && index > 0 )
		{
			i--;
		}
		else if( !up && index < quotas.size() - 1 )
		{
			i++;
		}
		quotas.remove(index);
		quotas.add(i, uq);

		configService.setProperties(config);
	}

	public SelectionsTable getBannedExtensions()
	{
		return bannedExtensions;
	}

	public SelectionsTable getUserQuotas()
	{
		return userQuotas;
	}

	public Link getAddUserQuotaLink()
	{
		return addUserQuotaLink;
	}

	@Override
	public Class<OneColumnLayoutModel> getModelClass()
	{
		return OneColumnLayoutModel.class;
	}
}
