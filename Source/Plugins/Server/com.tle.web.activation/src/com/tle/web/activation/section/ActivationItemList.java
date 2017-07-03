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

package com.tle.web.activation.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemActivationId;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.i18n.LangUtils;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.core.activation.ActivationResult;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.activation.ActivationItemListEntry;
import com.tle.web.itemlist.StdMetadataEntry;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;

@Bind
public class ActivationItemList
	extends
		AbstractItemList<ActivationItemListEntry, AbstractItemList.Model<ActivationItemListEntry>>
{
	private static final String DIV_PFX = "act_"; //$NON-NLS-1$

	@ResourceHelper
	private static PluginResourceHelper resources;

	@PlugKey("selectitem")
	private static Label LABEL_SELECT;
	@PlugKey("unselectitem")
	private static Label LABEL_UNSELECT;

	@EventFactory
	private EventGenerator events;
	@TreeLookup
	private ActivationSelectionSection selectionSection;

	@Inject
	private ActivationService activationService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private Provider<ActivationItemListEntry> entryFactory;

	private JSCallable selectCall;
	private JSCallable removeCall;

	@SuppressWarnings("nls")
	@Override
	protected void customiseListEntries(RenderContext context, List<ActivationItemListEntry> entries)
	{
		for( ActivationItemListEntry itemListEntry : entries )
		{
			ItemId itemId = itemListEntry.getItem().getItemId();
			Item item = itemListEntry.getItem();
			ItemActivationId actId = new ItemActivationId(itemId, itemListEntry.getActivationId());
			itemListEntry.getTag().setElementId(new SimpleElementId(DIV_PFX + actId.toString()));
			long activationId = Long.parseLong(itemListEntry.getActivationId());
			ActivateRequest request = activationService.getRequest(activationId);

			if( request != null )
			{
				Attachment attachment = findAttachment(item, request.getAttachment());

				ArrayList<Pair<LanguageBundle, LanguageBundle>> deets = new ArrayList<Pair<LanguageBundle, LanguageBundle>>();
				if( attachment == null )
				{
					deets.add(new Pair<LanguageBundle, LanguageBundle>(bundle(resources
						.getString("search.summary.attachment")), //$NON-NLS-1$
						LangUtils.createTextTempLangugageBundle("Unknown attachment " //$NON-NLS-1$
							+ request.getAttachment())));
				}
				else
				{
					TimeZone userZone = CurrentTimeZone.get();
					deets.add(new Pair<LanguageBundle, LanguageBundle>(bundle(resources
						.getString("search.summary.attachment")), bundle(attachment.getDescription())));
					deets.add(new Pair<LanguageBundle, LanguageBundle>(bundle(resources
						.getString("search.summary.course")), request.getCourse().getName()));
					deets.add(new Pair<LanguageBundle, LanguageBundle>(bundle(resources
						.getString("search.summary.from")), bundle(new LocalDate(request.getFrom(), userZone)
						.format(Dates.DATE_ONLY_FULL))));
					deets.add(new Pair<LanguageBundle, LanguageBundle>(bundle(resources
						.getString("search.summary.until")), bundle(new LocalDate(request.getUntil(), userZone)
						.format(Dates.DATE_ONLY_FULL))));
					deets.add(new Pair<LanguageBundle, LanguageBundle>(bundle(resources
						.getString("search.summary.status")), bundle(statusToString(request.getStatus()))));

				}

				for( Pair<LanguageBundle, LanguageBundle> deet : deets )
				{
					itemListEntry.addMetadata(new StdMetadataEntry(new BundleLabel(deet.getFirst(), bundleCache),
						new LabelRenderer(new BundleLabel(deet.getSecond(), bundleCache))));
				}
			}

			if( !selectionSection.isSelected(context, actId) )
			{
				HtmlLinkState link = new HtmlLinkState(LABEL_SELECT, new OverrideHandler(selectCall, itemId.getUuid(),
					itemId.getVersion(), itemListEntry.getActivationId()));
				itemListEntry.addRatingAction(new ButtonRenderer(link).showAs(ButtonType.SELECT));
			}
			else
			{
				HtmlLinkState link = new HtmlLinkState(LABEL_UNSELECT, new OverrideHandler(removeCall,
					itemId.getUuid(), itemId.getVersion(), itemListEntry.getActivationId()));
				itemListEntry.addRatingAction(new ButtonRenderer(link).showAs(ButtonType.UNSELECT));
				itemListEntry.setSelected(true);
			}
		}

		super.customiseListEntries(context, entries);
	}

	@EventHandlerMethod
	public void selectItem(SectionInfo info, String uuid, int version, String activationId)
	{
		ItemActivationId actId = new ItemActivationId(uuid, version, activationId);
		selectionSection.addSelection(info, actId);
		addAjaxDiv(info, actId);
	}

	private void addAjaxDiv(SectionInfo info, ItemActivationId actId)
	{
		AjaxRenderContext renderContext = info.getAttributeForClass(AjaxRenderContext.class);
		if( renderContext != null )
		{
			renderContext.addAjaxDivs(DIV_PFX + actId.toString());
		}
	}

	@EventHandlerMethod
	public void removeItem(SectionInfo info, String uuid, int version, String activationId)
	{
		ItemActivationId actId = new ItemActivationId(uuid, version, activationId);
		selectionSection.removeSelection(info, actId);
		addAjaxDiv(info, actId);
	}

	@SuppressWarnings("nls")
	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		selectCall = selectionSection.getUpdateSelection(tree, events.getEventHandler("selectItem"));
		removeCall = selectionSection.getUpdateSelection(tree, events.getEventHandler("removeItem"));
	}

	protected LanguageBundle bundle(String text)
	{
		return LangUtils.createTextTempLangugageBundle(text);
	}

	protected Attachment findAttachment(Item item, String attachmentId)
	{
		return UnmodifiableAttachments.convertToMapUuid(item.getAttachments()).get(attachmentId);
	}

	protected String statusToString(int status)
	{
		String key = null;
		switch( status )
		{
			case ActivateRequest.TYPE_ACTIVE:
				key = resources.key("status.active"); //$NON-NLS-1$
				break;

			case ActivateRequest.TYPE_INACTIVE:
				key = resources.key("status.inactive"); //$NON-NLS-1$
				break;

			case ActivateRequest.TYPE_PENDING:
				key = resources.key("status.pending"); //$NON-NLS-1$
				break;

			default:
				break;
		}

		return CurrentLocale.get(key);
	}

	@Override
	public ActivationItemListEntry addItem(SectionInfo info, Item item, FreetextResult resultData)
	{
		if( resultData instanceof ActivationResult )
		{
			ActivationResult result = (ActivationResult) resultData;
			ActivationItemListEntry entry = createItemListEntry(info, item, resultData);
			entry.setActivationId(result.getActivationId());
			addListItem(info, entry);
			return entry;
		}
		else
		{
			throw new RuntimeException("serious problem"); //$NON-NLS-1$
		}
	}

	@SuppressWarnings("nls")
	@Override
	protected Set<String> getExtensionTypes()
	{
		return Collections.singleton("activation");
	}

	@Override
	protected ActivationItemListEntry createItemListEntry(SectionInfo info, Item item, FreetextResult result)
	{
		ActivationItemListEntry activationItemListItem = entryFactory.get();
		activationItemListItem.setInfo(info);
		activationItemListItem.setItem(item);
		activationItemListItem.setFreetextData(result);
		return activationItemListItem;
	}
}
