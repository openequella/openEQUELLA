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

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.item.service.ItemService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewItemUrlFactory;

@SuppressWarnings("nls")
public class VersionsContentSection extends AbstractContentSection<Object>
{
	@PlugKey("summary.content.versions.pagetitle")
	private static Label TITLE_LABEL;
	@PlugKey("summary.content.versions.column.version")
	private static Label LABEL_VERSION;
	@PlugKey("summary.content.versions.column.itemtitle")
	private static Label LABEL_ITEM_TITLE;
	@PlugKey("summary.content.versions.column.status")
	private static Label LABEL_STATUS;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private ItemService itemService;
	@Inject
	private ViewItemUrlFactory viewItemUrlFactory;

	@Component(name = "v")
	private Table versionsTable;

	private SubmitValuesFunction versionsClickedFunc;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		versionsClickedFunc = events.getSubmitValuesFunction("versionClicked");
		versionsTable.setColumnHeadings(LABEL_VERSION, LABEL_ITEM_TITLE, LABEL_STATUS);
		versionsTable.setColumnSorts(Sort.PRIMARY_DESC, Sort.NONE, Sort.NONE);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		ItemSectionInfo itemInfo = ParentViewItemSectionUtils.getItemInfo(context);

		final List<Item> items = itemService.getVersionDetails(itemInfo.getItem().getUuid());
		final TableState versionTableState = versionsTable.getState(context);
		versionTableState.addClass("versions");
		for( Item item : items )
		{
			final int version = item.getVersion();

			final HtmlLinkState linkState = new HtmlLinkState(new OverrideHandler(versionsClickedFunc, version));
			linkState.setLabel(new BundleLabel(item.getName(), item.getUuid(), bundleCache));

			TableRow row = versionTableState.addRow(version, linkState, item.getStatus());
			row.setSortData(version, null, null);
		}

		addDefaultBreadcrumbs(context, itemInfo, TITLE_LABEL);
		displayBackButton(context);

		return viewFactory.createResult("viewitem/summary/content/versions.ftl", context);
	}

	@EventHandlerMethod
	public void versionClicked(SectionInfo info, int version)
	{
		final String uuid = ParentViewItemSectionUtils.getItemInfo(info).getItemId().getUuid();
		info.forwardToUrl(viewItemUrlFactory.createItemUrl(info, new ItemId(uuid, version)).getHref());
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return viewFactory.createResult("viewitem/summary/help/versions.ftl", this);
	}

	public Table getVersionsTable()
	{
		return versionsTable;
	}
}
