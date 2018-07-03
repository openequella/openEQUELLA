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

package com.tle.web.viewurl.attachments.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.web.sections.equella.annotation.PlugKey;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.filesystem.FileSystemConstants;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.item.AttachmentUtils;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.service.ItemResolver;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.FileSystemService;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.mimetypes.service.WebMimeTypeService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.stream.ContentStream;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;
import com.tle.web.viewable.servlet.ThumbServlet.GalleryParameter;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.FilestoreBookmark;
import com.tle.web.viewurl.ViewAttachmentUrl;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.viewurl.resource.AbstractWrappedResource;

@NonNullByDefault
@Bind(AttachmentResourceService.class)
@Singleton
public class AttachmentResourceServiceImpl implements AttachmentResourceService
{
	@Inject
	private PluginTracker<AttachmentResourceExtension<IAttachment>> attachmentResources;

	@Nullable
	private volatile Map<String, List<Extension>> extensionMap;

	private Object mapLock = new Object();

	@Inject
	private ViewableItemResolver viewItemResolver;
	@Inject
	private ItemResolver itemResolver;
	@Inject
	private WebMimeTypeService mimeService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public ViewableResource getViewableResource(SectionInfo info, ViewableItem viewableItem, IAttachment attachment)
	{
		ViewableResource currentResource = new ViewAttachmentResource(info, viewableItem, attachment);
		Map<String, List<Extension>> map = getExtensionMap();
		String type = getTypeForAttachment(attachment);
		List<Extension> extensions = map.get(type);

		if( isRestricted(viewableItem, attachment) || extensions == null )
		{
			currentResource.setAttribute(ViewableResource.KEY_HIDDEN, true);
		}
		if( extensions != null )
		{
			for( Extension extension : extensions )
			{
				currentResource = attachmentResources.getBeanByExtension(extension).process(info, currentResource,
					attachment);
			}
		}
		return currentResource;
	}

	@Override
	public URI getPackageZipFileUrl(Item item, Attachment attachment)
	{
		try
		{
			// Need the_IMS folder, not the _SCORM folder ...?
			String zipFileUrl = institutionService.institutionalise("file/" + item.getItemId() + '/')
				+ FileSystemConstants.IMS_FOLDER + '/'
				+ URIUtil.encodePath(attachment.getUrl(), Charsets.UTF_8.toString());
			return new URI(zipFileUrl);
		}
		catch( URISyntaxException | URIException e )
		{
			throw new RuntimeException(e);
		}
	}

	private boolean isRestricted(ViewableItem<IItem<?>> viewableItem, IAttachment attachment)
	{
		if( attachment.isRestricted() )
		{
			// Dirty.  This blows up for preview items et al.
			IItem<?> item;
			try
			{
				item = viewableItem.getItem();
			}
			catch( UnsupportedOperationException unsupp )
			{
				return false;
			}
			return itemResolver.checkRestrictedAttachment(item, attachment, viewableItem.getItemExtensionType());
		}
		return false;
	}

	public static String getTypeForAttachment(IAttachment attachment)
	{
		AttachmentType attachType = attachment.getAttachmentType();
		String type = attachType.name().toLowerCase();
		if( attachType == AttachmentType.CUSTOM )
		{
			type += '/' + ((CustomAttachment) attachment).getType().toLowerCase();
		}
		return type;
	}

	private Map<String, List<Extension>> getExtensionMap()
	{
		if( extensionMap == null )
		{
			synchronized( mapLock )
			{
				extensionMap = new HashMap<String, List<Extension>>();
				List<Extension> extensions = attachmentResources.getExtensions();
				for( Extension extension : extensions )
				{
					Collection<Parameter> typeParams = extension.getParameters("type"); //$NON-NLS-1$
					for( Parameter parameter : typeParams )
					{
						String typeString = parameter.valueAsString();
						List<Extension> perTypeList = extensionMap.get(typeString);
						if( perTypeList == null )
						{
							perTypeList = new ArrayList<Extension>();
							extensionMap.put(typeString, perTypeList);
						}
						perTypeList.add(extension);
					}
				}
			}
		}
		return extensionMap;
	}

	public class ViewAttachmentResource implements ViewableResource
	{
		private final Map<Object, Object> attributes = new HashMap<Object, Object>();
		private final ViewableItem viewableItem;
		@Nullable
		private final IAttachment attachment;
		private final SectionInfo info;
		private boolean customThumb;
		private ViewableResource topLevel;

		public ViewAttachmentResource(SectionInfo info, ViewableItem viewableItem, @Nullable IAttachment attachment)
		{
			this.info = info;
			this.viewableItem = viewableItem;
			this.attachment = attachment;
			if( attachment != null )
			{
				customThumb = !Check.isEmpty(attachment.getThumbnail())
					&& !attachment.getThumbnail().equals("suppress");
			}
			topLevel = this;
		}

		@Override
		public ViewItemUrl createDefaultViewerUrl()
		{
			ViewItemUrl vurl = viewItemResolver.createViewItemUrl(info, viewableItem,
				viewableItem.getItemExtensionType());
			vurl.add(new ViewAttachmentUrl(attachment.getUuid()));
			vurl.addFlag(ViewItemUrl.FLAG_NO_SELECTION);
			return vurl;
		}

		@Override
		public boolean isExternalResource()
		{
			return false;
		}

		@Override
		@SuppressWarnings("nls")
		public Bookmark createCanonicalUrl()
		{
			if( topLevel.hasContentStream() )
			{
				return new SimpleBookmark(topLevel.getViewableItem().getItemdir() + "?attachment.uuid="
					+ topLevel.getAttachment().getUuid() + "&attachment.stream=true");
			}
			throw new SectionsRuntimeException("Must either have a canonical url or a stream");
		}

		@Override
		public boolean isCustomThumb()
		{
			return customThumb;
		}

		@Override
		public ThumbRef getThumbnailReference(SectionInfo info, GalleryParameter gallery)
		{
			if( customThumb )
			{
				String thumbUrl = attachment.getThumbnail();
				if( gallery != null )
				{
					int lastIndex = 0;
					String replaceWith = "";
					switch( gallery )
					{
						case ORIGINAL:
							replaceWith = attachment.getUrl();
							break;
						case PREVIEW:
							lastIndex = thumbUrl.lastIndexOf(FileSystemService.THUMBNAIL_EXTENSION);
							replaceWith = FileSystemService.GALLERY_PREVIEW_EXTENSION;
							break;
						case THUMBNAIL:
							lastIndex = thumbUrl.lastIndexOf(FileSystemService.THUMBNAIL_EXTENSION);
							replaceWith = FileSystemService.GALLERY_THUMBNAIL_EXTENSION;
							break;
					}
					thumbUrl = new StringBuilder(thumbUrl).replace(lastIndex, thumbUrl.length(), replaceWith)
						.toString();
				}

				final FileHandle fileHandle = getViewableItem().getFileHandle();
				if( fileSystemService.fileExists(fileHandle, thumbUrl) )
				{
					return new ThumbRef(fileHandle, thumbUrl);
				}
				return new ThumbRef(true);
			}

			MimeEntry entry = mimeService.getEntryForMimeType(topLevel.getMimeType());
			return new ThumbRef(mimeService.getIconForEntry(entry));
		}

		@Override
		@SuppressWarnings("nls")
		public ImageRenderer createStandardThumbnailRenderer(Label label)
		{
			String source;
			if( customThumb )
			{
				ViewableItem viewableItem = getViewableItem();
				if( viewableItem.isItemForReal() )
				{
					ItemKey key = viewableItem.getItemId();
					source = institutionService.institutionalise(MessageFormat.format("thumbs/{0}/{1}/{2}",
						key.getUuid(), key.getVersion(), attachment.getUuid()));
				}
				else
				{
					Bookmark bm = viewableItem.createStableResourceUrl(attachment.getThumbnail());
					// assumption is the mother of all fuck ups
					if( bm instanceof FilestoreBookmark )
					{
						final FilestoreBookmark fsb = (FilestoreBookmark) bm;
						final String stagingUuid = fsb.getStagingUuid();
						if( stagingUuid != null )
						{
							// We add the .jpeg extension to fool the browser
							source = institutionService.institutionalise(MessageFormat.format("thumbs/{0}/$/{1}",
								stagingUuid, attachment.getUrl() + FileSystemService.THUMBNAIL_EXTENSION));
						}
						else
						{
							source = bm.getHref();
						}
					}
					else
					{
						source = bm.getHref();
					}
				}
			}
			else
			{
				MimeEntry entry = mimeService.getEntryForMimeType(topLevel.getMimeType());
				source = mimeService.getIconForEntry(entry).toString();
			}
			return new ImageRenderer(source, label);
		}

		@Override
		public ImageRenderer createGalleryThumbnailRenderer(Label label)
		{
			return new ImageRenderer(getGalleryUrl(false, false), label);
		}

		@Override
		public String getGalleryUrl(boolean preview, boolean original)
		{
			ItemKey key = getViewableItem().getItemId();
			GalleryParameter param = !preview ? GalleryParameter.THUMBNAIL
				: original ? GalleryParameter.ORIGINAL : GalleryParameter.PREVIEW;
			return institutionService.institutionalise(MessageFormat.format("thumbs/{0}/{1}/{2}?gallery={3}",
				key.getUuid(), key.getVersion(), attachment.getUuid(), param.toString().toLowerCase()));
		}

		@Override
		public ImageRenderer createVideoThumbnailRenderer(Label label, TagState tag)
		{
			return new ImageRenderer(tag, getGalleryUrl(false, false), label);
		}

		@Override
		public IAttachment getAttachment()
		{
			return attachment;
		}

		@SuppressWarnings("unchecked")
		@Nullable
		@Override
		public <T> T getAttribute(Object key)
		{
			return (T) attributes.get(key);
		}

		@Override
		public String getDescription()
		{
			return attachment.getDescription();
		}

		@Override
		public String getMimeType()
		{
			return null;
		}

		@Override
		public ViewAuditEntry getViewAuditEntry()
		{
			return null;
		}

		@Override
		public ViewableItem getViewableItem()
		{
			return viewableItem;
		}

		@Override
		public boolean hasContentStream()
		{
			return false;
		}

		@Override
		public void setAttribute(Object key, Object value)
		{
			attributes.put(key, value);
		}

		@Override
		public boolean getBooleanAttribute(Object key)
		{
			Boolean attr = getAttribute(key);
			return attr != null ? attr : false;
		}

		@Override
		public SectionInfo getInfo()
		{
			return info;
		}

		@SuppressWarnings("nls")
		@Override
		public ContentStream getContentStream()
		{
			throw new SectionsRuntimeException("Must be wrapped with an implementation");
		}

		@Override
		public String getFilepath()
		{
			return "";
		}

		@Override
		public void wrappedBy(ViewableResource resource)
		{
			topLevel = resource;
		}

		@Override
		public String getDefaultViewer()
		{
			return attachment.getViewer();
		}

		@Override
		public boolean isDisabled()
		{
			return false;
		}

		@Override
		public List<AttachmentDetail> getCommonAttachmentDetails()
		{
			return null;
		}

		@Override
		public List<AttachmentDetail> getExtraAttachmentDetails()
		{
			List<AttachmentDetail> details = Lists.newArrayList();

			Map<String, Object> attData = attachment.getDataAttributes();
			if( !Check.isEmpty(attData) )
			{
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, Object> customData = (LinkedHashMap<String, Object>) attData
					.get(AttachmentUtils.CUSTOM_DISPLAY_KEY);

				if( !Check.isEmpty(customData) )
				{
					for( Entry<String, Object> entry : customData.entrySet() )
					{
						String key = entry.getKey();
						String value = entry.getValue().toString();
						details.add(new AttachmentDetail(new TextLabel(key + ":"), new TextLabel(value)));
					}
				}
			}

			return details;
		}
	}

	@Override
	public PathViewableResource createPathResource(SectionInfo info, ViewableItem viewableItem, String path,
		@Nullable IAttachment attachment)
	{
		return createPathResource(info, viewableItem, path, path, mimeService.getMimeTypeForFilename(path), attachment);
	}

	@Override
	public PathViewableResource createPathResource(SectionInfo info, ViewableItem viewableItem, String path,
		String description, @Nullable String mimeType, @Nullable IAttachment attachment)
	{
		return new PathViewableResource(new ViewAttachmentResource(info, viewableItem, attachment), path, description,
			mimeType);
	}

	public class PathViewableResource extends AbstractWrappedResource
	{
		private String mimeType;
		private final String filepath;
		private String description;
		private int viewerFlags;

		public PathViewableResource(ViewableResource inner, String filepath, String description, String mimeType)
		{
			super(inner);
			this.mimeType = mimeType;
			this.filepath = filepath;
			this.description = description;
		}

		@SuppressWarnings("nls")
		@Override
		public List<AttachmentDetail> getCommonAttachmentDetails()
		{
			List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();
			IAttachment attachment = getAttachment();
			// Type
			commonDetails.add(makeDetail(new TextLabel(CurrentLocale.get("com.tle.web.viewurl.details.type")),
				new TextLabel(attachment.getAttachmentType().toString())));
			// Name
			commonDetails.add(makeDetail(new TextLabel(CurrentLocale.get("com.tle.web.viewurl.details.name")),
				new TextLabel(attachment.getDescription())));
			return commonDetails;
		}

		@Override
		public List<AttachmentDetail> getExtraAttachmentDetails()
		{
			return null;
		}

		@Override
		public Bookmark createCanonicalUrl()
		{
			return createDefaultViewerUrl();
		}

		@Override
		public boolean hasContentStream()
		{
			return false;
		}

		@Override
		public ViewItemUrl createDefaultViewerUrl()
		{
			final ViewableItem viewableItem = top.getViewableItem();
			final ViewItemUrl vurl = viewItemResolver.createViewItemUrl(getInfo(), viewableItem,
				UrlEncodedString.createFromFilePath(filepath), viewerFlags | ViewItemUrl.FLAG_NO_SELECTION,
				viewableItem.getItemExtensionType());
			return vurl;
		}

		@Override
		public String getMimeType()
		{
			return mimeType;
		}

		@Override
		public String getDescription()
		{
			return description;
		}

		public void setMimeType(String mimeType)
		{
			this.mimeType = mimeType;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public int getViewerFlags()
		{
			return viewerFlags;
		}

		public void setViewerFlags(int viewerFlags)
		{
			this.viewerFlags = viewerFlags;
		}
	}
}
