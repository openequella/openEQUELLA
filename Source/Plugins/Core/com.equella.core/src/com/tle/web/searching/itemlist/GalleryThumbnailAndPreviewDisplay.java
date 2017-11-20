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

package com.tle.web.searching.itemlist;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.collection.AttachmentConfigConstants;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.FilestoreContentFilter;
import com.tle.web.viewitem.service.FileFilterService;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@NonNullByDefault
@Bind
public class GalleryThumbnailAndPreviewDisplay extends AbstractPrototypeSection<Object>
	implements
		ItemlikeListEntryExtension<Item, StandardItemListEntry>
{
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private FileFilterService filters;
	@Inject
	private TLEAclManager aclManager;

	@Override
	public ProcessEntryCallback<Item, StandardItemListEntry> processEntries(final RenderContext context,
		List<StandardItemListEntry> entries, ListSettings<StandardItemListEntry> listSettings)
	{
		listSettings.setHilightedWords(null);

		return new ProcessEntryCallback<Item, StandardItemListEntry>()
		{
			@SuppressWarnings("nls")
			@Override
			public void processEntry(StandardItemListEntry entry)
			{
				final Item item = entry.getItem();
				final String thumb = item.getThumb();
				if( thumb.startsWith("custom:") )
				{
					final String thumbUuid = thumb.split(":")[1];

					final ViewableItem<?> viewItem = entry.getViewableItem();
					final Attachment attachment = (Attachment) viewItem.getAttachmentByUuid(thumbUuid);
					final ViewableResource viewableResource = attachmentResourceService.getViewableResource(context,
						viewItem, attachment);
					entry.addThumbnail(viewableResource.createGalleryThumbnailRenderer(entry.getTitleLabel()));
					if( canUserViewAttachment(item, attachment) )
					{
						entry.addExtras(attachPreview(attachment, viewableResource));
					}
				}
				else if( thumb.equals("default") )
				{
					for( ViewableResource viewableResource : entry.getViewableResources() )
					{
						final Attachment attachment = (Attachment) viewableResource.getAttachment();
						final String mimeType = mimeTypeService.getMimeEntryForAttachment(attachment);
						if( mimeType != null && mimeType.startsWith("image/") )
						{
							final String attachmentThumb = attachment.getThumbnail();
							if( attachmentThumb != null && !attachmentThumb.equals("suppress") )
							{

								entry.addThumbnail(
									viewableResource.createGalleryThumbnailRenderer(entry.getTitleLabel()));
								if( canUserViewAttachment(item, attachment) )
								{
									entry.addExtras(attachPreview(attachment, viewableResource));
								}
								break;
							}
						}
					}
				}
			}
		};
	}

	private boolean canUserViewAttachment(Item item, Attachment attach)
	{
		boolean canView = true;

		for( FilestoreContentFilter filter : filters.getFilters() )
		{
			canView = filter.canView(item, attach);
			if( !canView )
			{
				break;
			}
		}
		if( attach.isRestricted() && aclManager.filterNonGrantedPrivileges(item,
			Collections.singleton(AttachmentConfigConstants.VIEW_RESTRICTED_ATTACHMENTS)).isEmpty() )
		{
			canView = false;
		}
		return canView;
	}

	private DivRenderer attachPreview(IAttachment att, ViewableResource viewableResource)
	{
		String thumbPath = att.getThumbnail();
		if( thumbPath == null || thumbPath.equals("suppress") )
		{
			return null;
		}
		int lastIndex = thumbPath.lastIndexOf(FileSystemService.THUMBNAIL_EXTENSION);
		String previewPath = new StringBuilder(thumbPath)
			.replace(lastIndex, thumbPath.length(), FileSystemService.GALLERY_PREVIEW_EXTENSION).toString();

		final ViewableItem vitem = viewableResource.getViewableItem();
		final ItemFile itemFile = itemFileService.getItemFile(vitem.getItemId(), null);
		String source;
		if( !fileSystemService.fileExists(itemFile, previewPath) )
		{
			source = viewableResource.getGalleryUrl(true, true);
		}
		else
		{
			source = viewableResource.getGalleryUrl(true, false);
		}

		DivRenderer previewDiv = new DivRenderer("onhover", null);
		previewDiv.setData("img-src", source);
		return previewDiv;
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);

	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	@Override
	public String getItemExtensionType()
	{
		return null;
	}

}
