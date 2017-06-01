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

package com.tle.web.cloud.attachment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.core.cloud.service.CloudService;
import com.tle.core.guice.Bind;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.cloud.viewable.CloudViewItemLinkFactory;
import com.tle.web.cloud.viewable.CloudViewItemUrlFactory;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.viewable.servlet.ThumbServlet.GalleryParameter;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;
import com.tle.web.viewurl.resource.AbstractWrappedResource;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
@Singleton
public class CloudAttachmentSummariser implements AttachmentResourceExtension<IAttachment>
{
	static
	{
		PluginResourceHandler.init(CloudAttachmentSummariser.class);
	}

	@PlugKey("attachment.details.type")
	private static Label TYPE;

	@Inject
	private CloudService cloudService;
	@Inject
	private CloudViewItemUrlFactory urlFactory;
	@Inject
	private CloudViewItemLinkFactory linkFactory;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, IAttachment attachment)
	{
		if( attachment instanceof CloudAttachment )
		{
			final CloudAttachment cloudAttach = (CloudAttachment) attachment;
			return new CloudAttachmentViewableResource(resource, cloudAttach, cloudService.getMimeType(cloudAttach));
		}
		return resource;
	}

	public class CloudAttachmentViewableResource extends AbstractWrappedResource
	{
		private final CloudAttachment attachment;
		@Nullable
		private final MimeEntry mimeType;

		public CloudAttachmentViewableResource(ViewableResource inner, CloudAttachment attachment,
			@Nullable MimeEntry mimeType)
		{
			super(inner);
			this.attachment = attachment;
			this.mimeType = mimeType;
		}

		@Override
		public boolean isExternalResource()
		{
			return true;
		}

		@Override
		public boolean hasContentStream()
		{
			return false;
		}

		@Override
		public Bookmark createCanonicalUrl()
		{
			return new SimpleBookmark(attachment.getViewUrl());
		}

		@Override
		public ThumbRef getThumbnailReference(SectionInfo info, GalleryParameter gallery)
		{
			// Only used by thumbs servlet, we can ignore
			throw new UnsupportedOperationException();
		}

		@Override
		public ImageRenderer createStandardThumbnailRenderer(Label label)
		{
			return new ImageRenderer(linkFactory.createThumbnailAttachmentLink(attachment, mimeType), label);
		}

		@Override
		public ViewItemUrl createDefaultViewerUrl()
		{
			final ViewItemUrl vurl = urlFactory.createItemUrl(getInfo(), (CloudViewableItem) getViewableItem(),
				attachment);
			vurl.addFlag(ViewItemUrl.FLAG_NO_SELECTION);
			return vurl;
		}

		@Nullable
		@Override
		public String getDefaultViewer()
		{
			return null;
		}

		@Nullable
		@Override
		public String getMimeType()
		{
			return (mimeType == null ? null : mimeType.getType());
		}

		@Override
		public List<AttachmentDetail> getCommonAttachmentDetails()
		{
			List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();

			// Type
			commonDetails.add(makeDetail(TYPE, new TextLabel(getMimeType())));

			return commonDetails;
		}
	}
}
