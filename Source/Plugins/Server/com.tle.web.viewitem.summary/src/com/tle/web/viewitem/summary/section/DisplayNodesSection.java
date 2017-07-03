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

package com.tle.web.viewitem.summary.section;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.DisplayNode;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.helper.ItemHelper.ItemHelperSettings;
import com.tle.core.xml.service.XmlService;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;

/*
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class DisplayNodesSection
	extends
		AbstractDisplayNodesSection<Item, AbstractDisplayNodesSection.DisplayNodesModel>
	implements
		ViewableChildInterface,
		DisplaySectionConfiguration
{
	@Inject
	private HtmlEditorService htmlEditorService;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private XmlService xmlService;
	@Inject
	private BundleCache bundleCache;

	@Nullable
	private String config;

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	protected ViewableItem<Item> getViewableItem(SectionInfo info)
	{
		return ParentViewItemSectionUtils.getItemInfo(info).getViewableItem();
	}

	@Nullable
	private List<DisplayNode> getDisplayNodes(SectionInfo info, ViewableItem<Item> vitem)
	{
		if( config != null )
		{
			Object fromXML = xmlService.deserialiseFromXml(getClass().getClassLoader(), config);
			return (List<DisplayNode>) fromXML;
		}
		return null;
	}

	@Nullable
	@Override
	protected List<AbstractDisplayNodesSection.Entry> getEntries(RenderEventContext context, ViewableItem<Item> vitem)
	{
		final List<DisplayNode> displayNodes = getDisplayNodes(context, vitem);
		if( displayNodes != null )
		{
			PropBagEx itemXml = itemHelper.convertToXml(new ItemPack<Item>(vitem.getItem(), vitem.getItemxml(), ""),
				new ItemHelperSettings(true));

			return fillValuesAndDoLayout(context, itemXml, displayNodes);
		}
		return null;
	}

	private List<Entry> fillValuesAndDoLayout(RenderEventContext context, final PropBagEx itemXml,
		final List<DisplayNode> nodes)
	{
		final List<Entry> results = new ArrayList<Entry>();
		for( DisplayNode node : nodes )
		{
			Integer truncLength = node.getTruncateLength();
			if( truncLength == null || truncLength == 0 )
			{
				truncLength = -1;
			}

			final String text = CurrentLocale.get(ItemHelper.getDisplayNodeValues(node, itemXml, null), null);
			if( !Check.isEmpty(text) )
			{
				final boolean html = node.isHTMLType();
				final SectionRenderable textRenderable = (html ? htmlEditorService.getHtmlRenderable(context, text)
					: new SimpleSectionResult(text));
				final Entry entry = new Entry(new BundleLabel(node.getTitle(), bundleCache), textRenderable,
					truncLength);
				entry.setFullspan(node.isSingleMode());
				if( html )
				{
					entry.setStyle(HtmlEditorService.DISPLAY_CLASS);
				}
				results.add(entry);
			}
		}
		return results;
	}

	@Override
	public void associateConfiguration(SummarySectionsConfig config)
	{
		this.config = config.getConfiguration();
	}

}
