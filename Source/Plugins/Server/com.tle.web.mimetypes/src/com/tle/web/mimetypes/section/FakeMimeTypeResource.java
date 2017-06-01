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

package com.tle.web.mimetypes.section;

import java.util.List;

import com.tle.beans.item.attachments.Attachment;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.stream.ContentStream;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.servlet.ThumbServlet.GalleryParameter;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;

public class FakeMimeTypeResource implements ViewableResource
{
	private final String mimeType;

	public FakeMimeTypeResource(String mimeType)
	{
		this.mimeType = mimeType;
	}

	@Override
	public String getMimeType()
	{
		if( mimeType == null )
		{
			return ""; //$NON-NLS-1$
		}
		return mimeType;
	}

	@Override
	public Bookmark createCanonicalUrl()
	{
		return null;
	}

	@Override
	public ContentStream getContentStream()
	{
		return null;
	}

	@Override
	public ViewItemUrl createDefaultViewerUrl()
	{
		return null;
	}

	@Override
	public ThumbRef getThumbnailReference(SectionInfo info, GalleryParameter gallery)
	{
		return null;
	}

	@Override
	public ImageRenderer createStandardThumbnailRenderer(Label label)
	{
		return null;
	}

	@Override
	public boolean isExternalResource()
	{
		return false;
	}

	@Override
	public Attachment getAttachment()
	{
		return null;
	}

	@Override
	public <T> T getAttribute(Object key)
	{
		return null;
	}

	@Override
	public boolean getBooleanAttribute(Object key)
	{
		return false;
	}

	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public String getFilepath()
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public SectionInfo getInfo()
	{
		return null;
	}

	@Override
	public boolean isCustomThumb()
	{
		return false;
	}

	@Override
	public ViewAuditEntry getViewAuditEntry()
	{
		return null;
	}

	@Override
	public ViewableItem getViewableItem()
	{
		return null;
	}

	@Override
	public boolean hasContentStream()
	{
		return false;
	}

	@Override
	public void setAttribute(Object key, Object value)
	{
		// nothing
	}

	@Override
	public void wrappedBy(ViewableResource resource)
	{
		// nothing
	}

	@Override
	public String getDefaultViewer()
	{
		return null;
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
		return null;
	}

	@Override
	public ImageRenderer createGalleryThumbnailRenderer(Label label)
	{
		return null;
	}

	@Override
	public ImageRenderer createVideoThumbnailRenderer(Label label, TagState tag)
	{
		return null;
	}

	@Override
	public String getGalleryUrl(boolean preview, boolean original)
	{
		return null;
	}
}