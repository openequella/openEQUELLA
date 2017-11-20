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

package com.tle.web.scorm;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.URLUtils;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.core.services.FileSystemService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;
import com.tle.web.viewurl.resource.AbstractRealFileResource;
import com.tle.web.viewurl.resource.SimpleUrlResource;

@Bind
@Singleton
public class ScormChildResource
	implements
		AttachmentResourceExtension<CustomAttachment>,
		RegisterMimeTypeExtension<Attachment>
{
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ViewItemUrlFactory urlFactory;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, CustomAttachment attachment)
	{
		String filename = attachment.getUrl();
		if( URLUtils.isAbsoluteUrl(filename) )
		{
			return new SimpleUrlResource(resource, filename, attachment.getDescription(), false);
		}

		ScormChildAttachmentResource attachmentResource = new ScormChildAttachmentResource(resource, filename,
			mimeService.getMimeTypeForFilename(filename));
		attachmentResource.setAttribute(ViewableResource.KEY_HIDDEN, true);
		return attachmentResource;
	}

	public class ScormChildAttachmentResource extends AbstractRealFileResource
	{
		public ScormChildAttachmentResource(ViewableResource inner, String filePath, String mimeType)
		{
			super(inner, filePath, mimeType, urlFactory, fileSystemService);
		}

		@Override
		public List<AttachmentDetail> getCommonAttachmentDetails()
		{
			return null;
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
