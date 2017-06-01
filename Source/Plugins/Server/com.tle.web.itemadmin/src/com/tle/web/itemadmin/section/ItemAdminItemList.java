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

package com.tle.web.itemadmin.section;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.guice.Bind;
import com.tle.web.itemlist.item.StandardItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@SuppressWarnings("nls")
@Bind
public class ItemAdminItemList extends StandardItemList
{
	private static final String DIV_PFX = "it_";

	@PlugKey("selectitem")
	private static Label LABEL_SELECT;
	@PlugKey("unselectitem")
	private static Label LABEL_UNSELECT;

	@PlugURL("js/wait.js")
	private static String JS_WAIT_URL;

	@EventFactory
	private EventGenerator events;
	@TreeLookup
	private ItemAdminSelectionSection selectionSection;

	private JSCallable selectCall;
	private JSCallable removeCall;

	@Override
	protected Set<String> getExtensionTypes()
	{
		return ImmutableSet.of(ItemlikeListEntryExtension.TYPE_STANDARD, "itemadmin");
	}

	@Override
	protected void customiseListEntries(RenderContext context, List<StandardItemListEntry> entries)
	{
		super.customiseListEntries(context, entries);
		for( StandardItemListEntry itemListEntry : entries )
		{
			ItemIdKey itemId = new ItemIdKey(itemListEntry.getItem());
			itemListEntry.getTag().setElementId(new SimpleElementId(DIV_PFX + itemId.toString()));

			if( !selectionSection.isSelected(context, itemId) )
			{
				HtmlLinkState link = new HtmlLinkState(LABEL_SELECT, new OverrideHandler(selectCall, itemId));
				itemListEntry.addRatingAction(new ButtonRenderer(link).showAs(ButtonType.SELECT));
			}
			else
			{
				HtmlLinkState link = new HtmlLinkState(LABEL_UNSELECT, new OverrideHandler(removeCall, itemId));
				itemListEntry.addRatingAction(new ButtonRenderer(link).showAs(ButtonType.UNSELECT));
				itemListEntry.setSelected(true);
			}
		}
		context.getPreRenderContext().addJs(JS_WAIT_URL);
	}

	@EventHandlerMethod
	public void selectItem(SectionInfo info, ItemIdKey itemId)
	{
		selectionSection.addSelection(info, itemId);
		addAjaxDiv(info, itemId);
	}

	private void addAjaxDiv(SectionInfo info, ItemIdKey itemId)
	{
		AjaxRenderContext renderContext = info.getAttributeForClass(AjaxRenderContext.class);
		if( renderContext != null )
		{
			renderContext.addAjaxDivs(DIV_PFX + itemId.toString());
		}
	}

	@EventHandlerMethod
	public void removeItem(SectionInfo info, ItemIdKey itemId)
	{
		selectionSection.removeSelection(info, itemId);
		addAjaxDiv(info, itemId);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		selectCall = selectionSection.getUpdateSelection(tree, events.getEventHandler("selectItem"));
		removeCall = selectionSection.getUpdateSelection(tree, events.getEventHandler("removeItem"));
	}
}
