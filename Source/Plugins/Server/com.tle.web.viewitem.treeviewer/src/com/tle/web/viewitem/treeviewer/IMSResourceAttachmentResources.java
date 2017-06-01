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

package com.tle.web.viewitem.treeviewer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.attachments.Attachment;
import com.tle.common.FileSizeUtils;
import com.tle.common.URLUtils;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.core.services.FileSystemService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.stream.ContentStream;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;
import com.tle.web.viewurl.resource.AbstractRealFileResource;
import com.tle.web.viewurl.resource.SimpleUrlResource;

@Bind
@Singleton
public class IMSResourceAttachmentResources
	implements
		AttachmentResourceExtension<Attachment>,
		RegisterMimeTypeExtension<Attachment>
{
	static
	{
		PluginResourceHandler.init(IMSResourceAttachmentResources.class);
	}

	@PlugKey("details.type")
	private static Label TYPE;
	@PlugKey("details.name")
	private static Label NAME;
	@PlugKey("details.size")
	private static Label SIZE;

	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, Attachment attachment)
	{
		String filename = attachment.getUrl();
		if( URLUtils.isAbsoluteUrl(filename) )
		{
			return new SimpleUrlResource(resource, filename, attachment.getDescription(), false);
		}
		return new IMSResourceAttachmentResource(resource, filename, mimeService.getMimeTypeForFilename(filename));
	}

	/**
	 * Exactly the same as FileResource (Used in item summary)
	 */
	public class IMSResourceAttachmentResource extends AbstractRealFileResource
	{
		public IMSResourceAttachmentResource(ViewableResource inner, String filePath, String mimeType)
		{
			super(inner, filePath, mimeType, urlFactory, fileSystemService);
		}

		@Override
		public List<AttachmentDetail> getCommonAttachmentDetails()
		{
			List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();

			// Type
			commonDetails.add(makeDetail(TYPE, new TextLabel(getMimeType())));

			// Name
			commonDetails.add(makeDetail(NAME, new TextLabel(getDescription())));

			// Size
			if( hasContentStream() )
			{
				final ContentStream stream = getContentStream();
				if( stream.exists() )
				{
					final long length = stream.getEstimatedContentLength();
					String readableFileSize = length >= 0 ? FileSizeUtils.humanReadableFileSize(length) : "0";
					// makeDetail throws a nullPointerException if 2nd Label is
					// null, so ensure it isn't
					commonDetails.add(makeDetail(SIZE, new TextLabel(readableFileSize)));
				}
			}

			return commonDetails;
		}
	}

	@Override
	public String getMimeType(Attachment attachment)
	{
		String filename = attachment.getUrl();
		if( URLUtils.isAbsoluteUrl(filename) )
		{
			return MimeTypeConstants.MIME_LINK;
		}
		else
		{
			return mimeService.getMimeTypeForFilename(filename);
		}
	}
}
