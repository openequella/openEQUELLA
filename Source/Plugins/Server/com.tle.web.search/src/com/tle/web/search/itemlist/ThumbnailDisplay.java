package com.tle.web.search.itemlist;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.Constants;
import com.dytech.edge.exceptions.NotFoundException;
import com.google.common.collect.Sets;
import com.tle.beans.entity.itemdef.SearchDetails;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.ItemService;
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
		List<Item> items = AbstractItemlikeListEntry.getItems(entries);
		// FIXME: hack

		final Set<String> refIds = Sets.newHashSet(itemService.getNavReferencedAttachmentUuids(items));

		return new ProcessEntryCallback<Item, ItemListEntry>()
		{
			@SuppressWarnings("nls")
			@Override
			public void processEntry(ItemListEntry entry)
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

				IItem<?> item = entry.getItem();
				SearchDetails searchDetails = ((Item) item).getItemDefinition().getSearchDetails();

				String thumb = ((Item) item).getThumb();
				if( thumb.contains("none") )
				{
					return;
				}

				if( searchDetails == null || !searchDetails.isDisableThumbnail() )
				{
					if( viewableResources != null && viewableResources.size() > 0 )
					{
						for( ViewableResource viewableResource : viewableResources )
						{
							if( viewableResource.isCustomThumb()
								&& (!entry.getItem().getNavigationSettings().isManualNavigation() || refIds
									.contains(viewableResource.getAttachment().getUuid())) )
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
