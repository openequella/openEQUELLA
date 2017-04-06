package com.tle.web.viewable.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ViewableItemType;
import com.tle.core.guice.Bind;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.NewDefaultViewableItem;
import com.tle.web.viewable.ViewItemLinkFactory;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolverExtension;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
@Singleton
public class StandardViewableItemResolver implements ViewableItemResolverExtension
{
	@Inject
	private ViewableItemFactory viewableItemFactory;
	@Inject
	private ViewItemLinkFactory viewItemLinkFactory;
	@Inject
	private ViewItemUrlFactory viewItemUrlFactory;

	@Override
	public <I extends IItem<?>> ViewableItem<I> createViewableItem(I item)
	{
		final NewDefaultViewableItem viewableItem = viewableItemFactory.createNewViewableItem(item.getItemId());
		return (ViewableItem<I>) viewableItem;
	}

	@Override
	public <I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(I item, boolean latest,
		ViewableItemType viewableItemType)
	{
		return (ViewableItem<I>) viewableItemFactory.createIntegrationViewableItem(item.getItemId(), viewableItemType,
			latest);
	}

	@Override
	public <I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(ItemKey itemKey, boolean latest,
		ViewableItemType viewableItemType)
	{
		return (ViewableItem<I>) viewableItemFactory.createIntegrationViewableItem(itemKey, viewableItemType, latest);
	}

	@Override
	public <I extends IItem<?>> ViewItemUrl createViewItemUrl(SectionInfo info, ViewableItem<I> viewableItem)
	{
		return viewItemUrlFactory.createItemUrl(info, (ViewableItem<Item>) viewableItem);
	}

	@Override
	public <I extends IItem<?>> ViewItemUrl createViewItemUrl(SectionInfo info, ViewableItem<I> viewableItem,
		UrlEncodedString path, int flags)
	{
		return viewItemUrlFactory.createItemUrl(info, (ViewableItem<Item>) viewableItem, path, flags);
	}

	@Override
	public <I extends IItem<?>> Bookmark createThumbnailAttachmentLink(I item, boolean latest,
		@Nullable String attachmentUuid)
	{
		final ItemId itemId = (latest ? new ItemId(item.getUuid(), 0) : item.getItemId());
		return viewItemLinkFactory.createThumbnailAttachmentLink(itemId, attachmentUuid);
	}
}
