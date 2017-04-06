package com.tle.web.cloud.viewable.impl;

import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemKey;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.PathUtils;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.cloud.service.CloudService;
import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.tle.web.cloud.viewable.CloudViewItemLinkFactory;
import com.tle.web.mimetypes.service.WebMimeTypeService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.standard.model.SimpleBookmark;

@NonNullByDefault
@Bind(CloudViewItemLinkFactory.class)
@Singleton
@SuppressWarnings("nls")
public class CloudViewItemLinkFactoryImpl implements CloudViewItemLinkFactory
{
	private static final String CLOUD = "cloud/";

	@Inject
	private CloudService cloudService;
	@Inject
	private UrlService urlService;
	@Inject
	private WebMimeTypeService webMimeTypeService;

	@Override
	public Bookmark createCloudViewLink(ItemKey itemId)
	{
		return new CloudBookmark(itemId, null);
	}

	@Override
	public Bookmark createCloudViewAttachmentLink(ItemKey itemId, String attachmentUuid)
	{
		return new CloudBookmark(itemId, attachmentUuid);
	}

	@Override
	public Bookmark createThumbnailLink(CloudItem item)
	{
		return new SimpleBookmark(webMimeTypeService.getIconForEntry(
			webMimeTypeService.getEntryForMimeType("equella/item")).toString());
	}

	@Override
	public Bookmark createThumbnailAttachmentLink(CloudAttachment attachment, @Nullable MimeEntry mimeType)
	{
		final String thumbnail = attachment.getThumbnail();
		if( thumbnail == null )
		{
			final MimeEntry mime = (mimeType == null ? cloudService.getMimeType(attachment) : mimeType);
			return new SimpleBookmark(webMimeTypeService.getIconForEntry(mime).toString());
		}
		return new SimpleBookmark(thumbnail);
	}

	public class CloudBookmark implements Bookmark
	{
		protected final ItemKey itemKey;
		@Nullable
		protected final String attUuid;

		public CloudBookmark(ItemKey itemKey, @Nullable String attUuid)
		{
			this.itemKey = itemKey;
			this.attUuid = attUuid;
		}

		@Override
		public String getHref()
		{
			final String url;
			final String itemBase = PathUtils.urlPath(CLOUD, itemKey.toString());
			if( !Strings.isNullOrEmpty(attUuid) )
			{
				url = PathUtils.urlPath(itemBase, "attachment", attUuid);
			}
			else
			{
				url = itemBase;
			}
			return urlService.institutionalise(url);
		}
	}
}
