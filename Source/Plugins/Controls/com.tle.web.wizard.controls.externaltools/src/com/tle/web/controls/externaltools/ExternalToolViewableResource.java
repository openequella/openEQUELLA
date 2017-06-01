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

package com.tle.web.controls.externaltools;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;
import com.tle.common.Check;
import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.viewable.servlet.ThumbServlet.GalleryParameter;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.resource.AbstractWrappedResource;

public class ExternalToolViewableResource extends AbstractWrappedResource
{
	public ExternalToolViewableResource(ViewableResource resource, String thumbUrl)
	{
		super(resource);
		this.thumbUrl = thumbUrl;
	}

	static
	{
		PluginResourceHandler.init(ExternalToolViewableResource.class);
	}

	@PlugKey("externaltoolviewer.unsupported.msg")
	private static String UNSUPPORTED_MSG;

	@PlugKey("externaltoolresource.details.type")
	private static Label TYPE_LABEL;
	@PlugKey("externaltoolresource.details.mimetype")
	private static Label MIMETYPE_LABEL;

	private String thumbUrl;

	@Override
	public List<AttachmentDetail> getCommonAttachmentDetails()
	{
		List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();
		commonDetails.add(makeDetail(TYPE_LABEL, MIMETYPE_LABEL));

		return commonDetails;
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
	public String getMimeType()
	{
		return ExternalToolConstants.MIME_TYPE;
	}

	@Override
	public boolean isCustomThumb()
	{
		return !Check.isEmpty(getThumbUrl());
	}

	/**
	 * We can't afford to return null here, so we're assuming an ImageRenderer
	 * with a null String Source is safe, noting that attachments when created
	 * should default to the mimetype icon URL.
	 */
	@Override
	public ImageRenderer createStandardThumbnailRenderer(Label alt)
	{
		return new ImageRenderer(getThumbUrl(), alt);
	}

	private String getThumbUrl()
	{
		return thumbUrl;
	}

	@Override
	public ThumbRef getThumbnailReference(SectionInfo info, GalleryParameter gallery)
	{
		try
		{
			return new ThumbRef(new URL(getThumbUrl()));
		}
		catch( MalformedURLException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public Bookmark createCanonicalUrl()
	{
		throw new UnsupportedOperationException(CurrentLocale.get(UNSUPPORTED_MSG));
	}
}
