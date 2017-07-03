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

package com.tle.web.itemlist.item;

import java.text.DecimalFormat;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Pair;
import com.tle.core.item.service.ItemService;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.itemlist.StdMetadataEntry;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.ItemStatusKeys;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.ViewItemUrlFactory;

/**
 * Deals with real items, not IItem stuff
 * 
 * @author Aaron
 */
public abstract class AbstractItemListEntry extends AbstractItemlikeListEntry<Item> implements ItemListEntry
{
	private static final String KEY_ALLATTACHMENTS = "attachments";

	static
	{
		PluginResourceHandler.init(AbstractItemListEntry.class);
	}

	@PlugKey("lastupdated")
	private static Label LABEL_LASTUPDATED;
	@PlugKey("itemlist.inmoderation")
	private static Label LABEL_INMODERATION;
	@PlugKey("itemlist.status.status")
	private static Label LABEL_STATUS;
	@PlugKey("itemlist.status.relevance")
	private static Label LABEL_RELEVANCE;
	@PlugKey("itemlist.keywordfound")
	private static Label LABEL_KEYWORDFOUND;

	@Inject
	private ViewItemUrlFactory itemUrls;
	@Inject
	private ViewableItemFactory viewableItemFactory;
	@Inject
	private HtmlEditorService htmlEditorService;
	@Inject
	private ItemService itemService;

	@Override
	protected Bookmark getTitleLink()
	{
		final Item item = getItem();
		if( item != null )
		{
			return itemUrls.createItemUrl(info, item.getItemId());
		}
		return new SimpleBookmark("#");
	}

	@Override
	protected ViewableItem<Item> createViewableItem()
	{
		final Item item = getItem();
		if( item != null )
		{
			return viewableItemFactory.createNewViewableItem(item.getItemId());
		}
		return null;
	}

	@Override
	protected UnmodifiableAttachments loadAttachments()
	{
		Multimap<Item, Attachment> attachmentsForItems = listSettings.getAttribute(KEY_ALLATTACHMENTS);
		if( attachmentsForItems == null )
		{
			List<ItemListEntry> entries2 = (List<ItemListEntry>) listSettings.getEntries();
			attachmentsForItems = itemService
				.getAttachmentsForItems(AbstractItemlikeListEntry.<Item>getItems(entries2));
			listSettings.setAttribute(KEY_ALLATTACHMENTS, attachmentsForItems);
		}
		return new UnmodifiableAttachments(Lists.<IAttachment>newArrayList(attachmentsForItems.get(getItem())));
	}

	@Override
	protected void setupMetadata(RenderContext context)
	{
		final Item item = getItem();
		if( item != null )
		{
			List<Pair<LanguageBundle, LanguageBundle>> details = item.getSearchDetails();

			if( details != null )
			{
				for( Pair<LanguageBundle, LanguageBundle> detail : details )
				{
					final SectionRenderable html = htmlEditorService.getHtmlRenderable(context,
						new BundleLabel(detail.getSecond(), bundleCache).setHtml(true).getText());
					final SectionRenderable renderable = new SpanRenderer(HtmlEditorService.DISPLAY_CLASS, html);
					entries.add(new StdMetadataEntry(new BundleLabel(detail.getFirst(), bundleCache), renderable));
				}
			}
			List<Object> statusMeta = Lists.newArrayList();
			final ItemStatus status = item.getStatus();
			if( status != ItemStatus.PERSONAL )
			{
				statusMeta.add(getStatusLabel());
			}

			final SectionRenderable lastModified = getLastModified();
			if( lastModified != null )
			{
				statusMeta.add(SectionUtils.convertToRenderer(LABEL_LASTUPDATED, lastModified));
			}
			addDelimitedMetadata(LABEL_STATUS, statusMeta);
			if( item.isModerating() )
			{
				addDelimitedMetadata(LABEL_INMODERATION, getTimeRenderer(item.getModeration().getStart()));
			}

			List<Object> relevanceMeta = Lists.newArrayList();
			FreetextResult freetextResult = getFreetextData();

			if( freetextResult != null && freetextResult.isSortByRelevance() )
			{
				DecimalFormat df = new DecimalFormat("#0.###");
				String strRelevance = df.format(freetextResult.getRelevance());
				relevanceMeta.add(strRelevance);
			}

			if( freetextResult != null && freetextResult.isKeywordFoundInAttachment() )
			{
				relevanceMeta.add(LABEL_KEYWORDFOUND);
			}

			if( !relevanceMeta.isEmpty() )
			{
				addDelimitedMetadata(LABEL_RELEVANCE, relevanceMeta);
			}
		}
	}

	@Override
	public int getRating()
	{
		Item item = getItem();
		if( item != null )
		{
			int rating = (int) item.getRating();
			if( rating < 0 )
			{
				return 0;
			}
			if( rating > 5 )
			{
				return 5;
			}
			return rating;
		}
		return 0;
	}

	public Label getStatusLabel()
	{
		Item item = getItem();
		if( item != null )
		{
			return new KeyLabel(ItemStatusKeys.get(getItem().getStatus()));
		}
		return null;
	}
}