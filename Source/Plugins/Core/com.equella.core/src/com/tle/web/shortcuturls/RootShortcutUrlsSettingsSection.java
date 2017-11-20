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

package com.tle.web.shortcuturls;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.settings.standard.ShortcutUrls;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.shortcuturls.RootShortcutUrlsSettingsSection.ShortcutUrlsSettingsModel;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
public class RootShortcutUrlsSettingsSection extends OneColumnLayout<ShortcutUrlsSettingsModel>
{
	@PlugKey("shortcuts.page.title")
	private static Label LABEL_TITLE;
	@PlugKey("shortcuts.table.heading.text")
	private static Label LABEL_SHORTCUT_TEXT;
	@PlugKey("shortcuts.table.heading.url")
	private static Label LABEL_SHORTCUT_URL;
	@PlugKey("shortcuts.table.emptylist")
	private static Label LABEL_EMPTY;
	@PlugKey("shortcuts.table.delete")
	private static Label DELETE_LABEL;
	@PlugKey("shortcuts.table.confirm.delete")
	private static Confirm CONFIRM_DELETE;

	@EventFactory
	private EventGenerator events;

	@Inject
	private ConfigurationService configService;
	@Inject
	private ShortcutUrlsSettingsPrivilegeTreeProvider securityProvider;

	@Component
	private SelectionsTable shortcutsTable;
	@Inject
	@Component
	private AddShortcutUrlDialog addShortcutUrlDialog;
	@PlugKey("shortcuts.link.add")
	@Component
	private Link addShortcutUrlLink;

	private JSCallable deleteFunc;

	@Override
	public void registered(final String id, SectionTree tree)
	{
		super.registered(id, tree);
		addShortcutUrlDialog.setOkCallback(new ReloadFunction());
		addShortcutUrlLink.setClickHandler(addShortcutUrlDialog.getOpenFunction());

		deleteFunc = events.getSubmitValuesFunction("delete");

		shortcutsTable.setColumnHeadings(LABEL_SHORTCUT_TEXT, LABEL_SHORTCUT_URL, null);
		shortcutsTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.SORTABLE_ASC, Sort.NONE);
		shortcutsTable.setSelectionsModel(new ShortcutURLModel());
		shortcutsTable.setNothingSelectedText(LABEL_EMPTY);
		shortcutsTable.setAddAction(addShortcutUrlLink);
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext context)
	{
		securityProvider.checkAuthorised();
		getModel(context).setBaseUrl(CurrentInstitution.get().getUrl());
		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "shortcuturlssettings.ftl", this));
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(LABEL_TITLE);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	private ShortcutUrls getShortcutUrls()
	{
		return configService.getProperties(new ShortcutUrls());
	}

	@EventHandlerMethod
	public void delete(SectionInfo info, String key)
	{
		ShortcutUrls shortcutUrls = getShortcutUrls();
		shortcutUrls.getShortcuts().remove(key);
		configService.setProperties(shortcutUrls);
	}

	@Override
	public Class<RootShortcutUrlsSettingsSection.ShortcutUrlsSettingsModel> getModelClass()
	{
		return ShortcutUrlsSettingsModel.class;
	}

	private class ShortcutURLModel extends DynamicSelectionsTableModel<Pair<String, String>>
	{
		@Override
		protected List<Pair<String, String>> getSourceList(SectionInfo info)
		{
			List<Pair<String, String>> shortcutUrls = Lists.newArrayList();
			Map<String, String> scUrls = getShortcutUrls().getShortcuts();
			for( Entry<String, String> sc : scUrls.entrySet() )
			{
				shortcutUrls.add(new Pair<String, String>(sc.getKey(), sc.getValue()));
			}

			return shortcutUrls;
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, Pair<String, String> shortcutUrl,
			List<SectionRenderable> actions, int index)
		{
			String shortcut = shortcutUrl.getFirst();
			String url = shortcutUrl.getSecond();
			selection.setName(new TextLabel(shortcut));

			TableCell urlCell = new TableCell(url);
			urlCell.addClass("middle");
			selection.getCells().add(urlCell);

			actions.add(
				makeRemoveAction(DELETE_LABEL, new OverrideHandler(deleteFunc, shortcut).addValidator(CONFIRM_DELETE)));
		}
	}

	public static class ShortcutUrlsSettingsModel extends OneColumnLayout.OneColumnLayoutModel
	{
		private String baseUrl;

		public String getBaseUrl()
		{
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl)
		{
			this.baseUrl = baseUrl;
		}
	}

	public SelectionsTable getShortcutsTable()
	{
		return shortcutsTable;
	}
}
