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

package com.tle.web.search.selection;

import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemId;
import com.tle.common.settings.standard.QuickContributeAndVersionSettings;
import com.tle.core.item.service.ItemResolver;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.itemlist.item.ItemlikeListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.selection.SelectedResourceKey;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.event.ItemSelectorEvent;
import com.tle.web.selection.event.ItemSelectorEventListener;
import com.tle.web.selection.section.SelectionSummarySection;
import com.tle.web.viewable.ViewableItem;

@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractSelectItemListExtension<I extends IItem<?>, LE extends ItemlikeListEntry<I>>
	extends
		AbstractPrototypeSection<Object>
	implements ItemlikeListEntryExtension<I, LE>, ItemSelectorEventListener
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(AbstractSelectItemListExtension.class);
	private static final IncludeFile INCLUDE = new IncludeFile(resources.url("scripts/selectitemlist.js"));
	private static final JSCallable SELECT_ITEM = new ExternallyDefinedFunction("selectItemList", 4, INCLUDE);

	private static final String DIV_PFX = "it_";

	@Inject
	private SelectionService selectionService;
	@Inject
	private ItemResolver itemResolver;
	@Inject
	private ConfigurationService configurationService;
	@Inject
	private IntegrationService integrationService;

	@EventFactory
	private EventGenerator events;

	private JSCallable selectCall;
	private JSCallable removeCall;
	private JSCallable selectCallOne;
	private JSCallable removeCallOne;

	protected abstract ViewableItem<I> getViewableItem(SectionInfo info, I item);

	@EventHandlerMethod
	public void select(SectionInfo info, ItemId itemId, String extensionType)
	{
		final I item = itemResolver.getItem(itemId, extensionType);
		selectionService.addSelectedItem(info, item, null, extensionType);
		addAjaxDiv(info, itemId);
	}

	/**
	 * Or de-select? :)
	 * 
	 * @param info
	 * @param itemId
	 */
	@EventHandlerMethod
	public void unselect(SectionInfo info, ItemId itemId, String extensionType)
	{
		selectionService.removeSelectedResource(info, new SelectedResourceKey(itemId, extensionType));
		addAjaxDiv(info, itemId);
	}

	@Nullable
	@Override
	public ProcessEntryCallback<I, LE> processEntries(final RenderContext context, List<LE> entries,
		ListSettings<LE> settings)
	{
		final SelectionSession session = selectionService.getCurrentSession(context);
		if( session == null || !session.isSelectItem() )
		{
			return null;
		}

		boolean selectButtonDisable = selectButtonDisable(context, session);
		if( selectButtonDisable )
		{
			return null;
		}

		final String extensionType = getItemExtensionType();
		return new ProcessEntryCallback<I, LE>()
		{
			@Override
			public void processEntry(LE entry)
			{
				final I item = entry.getItem();
				final ItemId itemId = item.getItemId();
				final TagState tag = entry.getTag();
				tag.setElementId(new SimpleElementId(DIV_PFX + itemId.toString()));

				if( session.containsResource(new SelectedResourceKey(itemId, extensionType), false) )
				{
					final HtmlLinkState hls = new HtmlLinkState(entry.getUnselectLabel());
					hls.setClickHandler(new OverrideHandler(session.isSelectMultiple() ? removeCall : removeCallOne,
						itemId, extensionType));

					entry.addRatingAction(
						new ButtonRenderer(hls).showAs(ButtonType.MINUS).addClass("button-expandable unselect"));
					entry.setSelected(true);
				}
				else
				{
					final JSCallable selectItemFunction = selectionService.getSelectItemFunction(context,
						getViewableItem(context, item));
					if( selectItemFunction != null )
					{
						entry.setSelectable(true);
						final HtmlLinkState hls = new HtmlLinkState(entry.getSelectLabel());
						hls.setClickHandler(
							Js.handler(SELECT_ITEM, Jq.$(hls), selectItemFunction, itemId, extensionType));
						entry.addRatingAction(
							new ButtonRenderer(hls).showAs(ButtonType.PLUS).addClass("button-expandable"));
					}
				}
			}
		};
	}

	private boolean selectButtonDisable(SectionInfo info, SelectionSession session)
	{
		final boolean inIntegrationSession = integrationService.isInIntegrationSession(info);
		final boolean disableSelectionButton = configurationService
			.getProperties(new QuickContributeAndVersionSettings()).isButtonDisable();

		if( inIntegrationSession )
		{
			if( session.isSelectItem() && !session.isSelectAttachments() && !session.isSelectPackage() )
			{
				return false;
			}
		}
		return disableSelectionButton;
	}

	@Override
	public void supplyFunction(SectionInfo info, ItemSelectorEvent event)
	{
		event.setFunction(event.getSession().isSelectMultiple() ? selectCall : selectCallOne);
	}

	private void addAjaxDiv(SectionInfo info, ItemId itemId)
	{
		final AjaxRenderContext renderContext = info.getAttributeForClass(AjaxRenderContext.class);
		if( renderContext != null )
		{
			renderContext.addAjaxDivs(DIV_PFX + itemId.toString());
		}
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
		tree.addListener(null, ItemSelectorEventListener.class, this);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		selectCallOne = events.getSubmitValuesFunction("select");
		removeCallOne = events.getSubmitValuesFunction("unselect");
		final SelectionSummarySection summary = tree.getAttribute(SelectionSummarySection.class);
		if( summary != null )
		{
			selectCall = summary.getUpdateSelection(tree, events.getEventHandler("select"));
			removeCall = summary.getUpdateSelection(tree, events.getEventHandler("unselect"));
		}
		else
		{
			selectCall = selectCallOne;
			removeCall = removeCallOne;
		}
	}
}
