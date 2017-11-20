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

package com.tle.web.search.itemlist;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.Constants;
import com.google.common.collect.Sets;
import com.tle.beans.entity.itemdef.SearchDetails;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.web.itemlist.item.AbstractItemlikeListEntry;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
public class ThumbnailDisplay implements ItemlikeListEntryExtension<Item, ItemListEntry>
{
	@Inject
	private ItemService itemService;

	@Override
	public ProcessEntryCallback<Item, ItemListEntry> processEntries(final RenderContext context,
		List<ItemListEntry> entries, ListSettings<ItemListEntry> settings)
	{
		final List<Item> items = AbstractItemlikeListEntry.getItems(entries);
		// FIXME: hack
		final Set<String> refIds = Sets.newHashSet(itemService.getNavReferencedAttachmentUuids(items));

		return new ProcessEntryCallback<Item, ItemListEntry>()
		{
			@SuppressWarnings("nls")
			@Override
			public void processEntry(ItemListEntry entry)
			{
				final IItem<?> iitem = entry.getItem();
				if( iitem != null )
				{
					final Item item = (Item) iitem;
					final String thumb = item.getThumb();
					if( thumb.contains("none") )
					{
						return;
					}

					final SearchDetails searchDetails = item.getItemDefinition().getSearchDetails();
					if( searchDetails == null || !searchDetails.isDisableThumbnail() )
					{
						List<ViewableResource> viewableResources;
						try
						{
							viewableResources = entry.getViewableResources();
						}
						catch( NotFoundException ex )
						{
							viewableResources = null;
						}

						if( viewableResources != null && viewableResources.size() > 0 )
						{
							for( ViewableResource viewableResource : viewableResources )
							{
								if( viewableResource.isCustomThumb()
									&& (!item.getNavigationSettings().isManualNavigation()
										|| refIds.contains(viewableResource.getAttachment().getUuid())) )
								{
									if( thumb.contains("default") || thumb.contains("initial") )
									{
										ImageRenderer image = viewableResource
											.createStandardThumbnailRenderer(new TextLabel(Constants.BLANK));
										if( image != null )
										{
											entry.addThumbnail(image);
										}
									}
									else if( thumb.contains("custom") )
									{
										if( thumb.split(":").length == 2 )
										{
											String attchmentUuid = thumb.split(":")[1];
											if( viewableResource.getAttachment().getUuid().equals(attchmentUuid) )
											{
												ImageRenderer image = viewableResource
													.createStandardThumbnailRenderer(new TextLabel(Constants.BLANK));
												entry.addThumbnail(image);
												break;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		};
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		// Do nothing (not a section)
	}

	@Override
	public String getItemExtensionType()
	{
		return null;
	}
}
