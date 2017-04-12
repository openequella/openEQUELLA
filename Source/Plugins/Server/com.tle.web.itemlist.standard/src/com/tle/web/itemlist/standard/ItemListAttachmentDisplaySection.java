package com.tle.web.itemlist.standard;

import java.util.Collections;

import com.google.inject.Inject;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.itemdef.AttachmentDisplay;
import com.tle.beans.entity.itemdef.SearchDetails;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.security.SecurityConstants;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.item.ItemService;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
public class ItemListAttachmentDisplaySection extends AbstractItemlikeListAttachmentDisplaySection<Item, ItemListEntry>
{
	@Inject
	private ViewableItemFactory viewItemFactory;
	@Inject
	private ItemService itemService;
	@Inject
	private TLEAclManager aclManager;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final AttachmentDisplayModel<Item> model = getModel(context);

		final Item item = getCachedItem(context, model.getItemId());
		final SearchDetails searchDetails = item.getItemDefinition().getSearchDetails();
		if( searchDetails != null )
		{
			final String display = searchDetails.getAttDisplay();
			if( !Check.isEmpty(display) )
			{
				final AttachmentDisplay attdisplay = AttachmentDisplay.valueOf(display);
				switch( attdisplay )
				{
					case STRUCTURED:
						return renderAttachments(context, item, true);
					case THUMBNAILS:
						return renderAttachments(context, item, false);
				}
			}
		}
		return super.renderHtml(context);
	}

	@Override
	protected boolean canSeeAttachments(Item item)
	{
		// No VIEW_ITEM privilege, no view attachment toggle
		return !aclManager.filterNonGrantedObjects(Collections.singleton(SecurityConstants.VIEW_ITEM),
			Collections.singleton(item)).isEmpty();
	}

	@Override
	protected SearchDetails getSearchDetails(Item item)
	{
		return item.getItemDefinition().getSearchDetails();
	}

	@Override
	protected Item getItem(ItemListEntry entry)
	{
		return entry.getItem();
	}

	@Override
	protected Item getItem(ItemId itemId)
	{
		return itemService.get(itemId);
	}

	@Override
	protected ViewableItem<Item> getViewableItem(Item item)
	{
		return viewItemFactory.createNewViewableItem(item.getItemId());
	}

	@Override
	public String getItemExtensionType()
	{
		return null;
	}
}