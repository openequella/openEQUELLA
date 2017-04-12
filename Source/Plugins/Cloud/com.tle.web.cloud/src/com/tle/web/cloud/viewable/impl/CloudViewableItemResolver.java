package com.tle.web.cloud.viewable.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Strings;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ViewableItemType;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.cloud.service.CloudService;
import com.tle.core.guice.Bind;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.cloud.viewable.CloudViewItemLinkFactory;
import com.tle.web.cloud.viewable.CloudViewItemUrlFactory;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolverExtension;
import com.tle.web.viewurl.ViewItemUrl;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
@Singleton
public class CloudViewableItemResolver implements ViewableItemResolverExtension
{
	@Inject
	private CloudService cloudService;
	@Inject
	private CloudViewItemUrlFactory viewItemUrlFactory;
	@Inject
	private CloudViewItemLinkFactory linkFactory;

	@Override
	public <I extends IItem<?>> ViewableItem<I> createViewableItem(I item)
	{
		return (ViewableItem<I>) new CloudViewableItem((CloudItem) item);
	}

	@Override
	public <I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(I item, boolean latest,
		ViewableItemType viewableItemType)
	{
		return (ViewableItem<I>) new CloudViewableItem((CloudItem) item, true);
	}

	@Override
	public <I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(ItemKey itemKey, boolean latest,
		ViewableItemType viewableItemType)
	{
		return (ViewableItem<I>) new CloudViewableItem(cloudService.getItem(itemKey.getUuid(),
			latest ? 0 : itemKey.getVersion()), true);
	}

	@Override
	public <I extends IItem<?>> ViewItemUrl createViewItemUrl(SectionInfo info, ViewableItem<I> viewableItem)
	{
		return viewItemUrlFactory.createItemUrl(info, (CloudViewableItem) viewableItem);
	}

	@Override
	public <I extends IItem<?>> ViewItemUrl createViewItemUrl(SectionInfo info, ViewableItem<I> viewableItem,
		UrlEncodedString path, int flags)
	{
		return viewItemUrlFactory.createItemUrl(info, (CloudViewableItem) viewableItem, flags);
	}

	@Nullable
	@Override
	public <I extends IItem<?>> Bookmark createThumbnailAttachmentLink(I item, boolean latest,
		@Nullable String attachmentUuid)
	{
		final CloudItem cloudItem = (CloudItem) item;
		if( attachmentUuid != null && !Strings.isNullOrEmpty(attachmentUuid) )
		{
			final CloudAttachment attachment = (CloudAttachment) new UnmodifiableAttachments(cloudItem)
				.getAttachmentByUuid(attachmentUuid);
			return linkFactory.createThumbnailAttachmentLink(attachment, null);
		}
		return linkFactory.createThumbnailLink(cloudItem);
	}
}
