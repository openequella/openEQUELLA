package com.tle.web.viewable.impl;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ViewableItemType;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;
import com.tle.web.viewable.ViewableItemResolverExtension;
import com.tle.web.viewurl.ViewItemUrl;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind(ViewableItemResolver.class)
@Singleton
public class ViewableItemResolverImpl implements ViewableItemResolver
{
	@Inject
	private PluginTracker<ViewableItemResolverExtension> resolverTracker;

	@Override
	public <I extends IItem<?>> ViewableItem<I> createViewableItem(I item, @Nullable String extensionType)
	{
		return getResolver(extensionType).createViewableItem(item);
	}

	@Override
	public <I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(I item, boolean latest,
		ViewableItemType viewableItemType, @Nullable String extensionType)
	{
		return getResolver(extensionType).createIntegrationViewableItem(item, latest, viewableItemType);
	}

	@Override
	public <I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(ItemKey itemKey, boolean latest,
		ViewableItemType viewableItemType, @Nullable String extensionType)
	{
		return getResolver(extensionType).createIntegrationViewableItem(itemKey, latest, viewableItemType);
	}

	@Override
	public <I extends IItem<?>> ViewItemUrl createViewItemUrl(SectionInfo info, ViewableItem<I> viewableItem,
		String extensionType)
	{
		return getResolver(extensionType).createViewItemUrl(info, viewableItem);
	}

	@Override
	public <I extends IItem<?>> ViewItemUrl createViewItemUrl(SectionInfo info, ViewableItem<I> viewableItem,
		UrlEncodedString path, int flags, String extensionType)
	{
		return getResolver(extensionType).createViewItemUrl(info, viewableItem, path, flags);
	}

	@Override
	@Nullable
	public <I extends IItem<?>> Bookmark createThumbnailAttachmentLink(I item, boolean latest,
		@Nullable String attachmentUuid, String extensionType)
	{
		return getResolver(extensionType).createThumbnailAttachmentLink(item, latest, attachmentUuid);
	}

	private ViewableItemResolverExtension getResolver(@Nullable String extensionType)
	{
		final String et = (extensionType == null ? "standard" : extensionType);
		final Map<String, ViewableItemResolverExtension> beanMap = resolverTracker.getBeanMap();
		final ViewableItemResolverExtension viewableItemResolverExtension = beanMap.get(et);
		if( viewableItemResolverExtension == null )
		{
			throw new Error("No viewable item resolver for extension type " + et);
		}
		return viewableItemResolverExtension;
	}
}
