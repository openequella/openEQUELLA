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

package com.tle.web.controls.googlebook;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.Check;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.selection.SelectionService;
import com.tle.web.viewable.servlet.ThumbServlet.GalleryParameter;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.resource.AbstractWrappedResource;

@SuppressWarnings("nls")
public class GoogleBookSearchViewableResource extends AbstractWrappedResource
{
	static
	{
		PluginResourceHandler.init(GoogleBookSearchViewableResource.class);
	}

	@PlugKey("gbook.details.type")
	private static KeyLabel TYPE;
	@PlugKey("gbook.details.mimetype")
	private static KeyLabel MIMETYPE;
	@PlugKey("gbook.details.title")
	private static KeyLabel TITLE;
	@PlugKey("gbook.details.pages")
	private static KeyLabel PAGES;
	// @PlugKey("details.authors")
	// private static KeyLabel AUTHORS;
	@PlugKey("gbook.details.published")
	private static KeyLabel PUBLISHED;

	private final CustomAttachment googleBookAttachment;

	public GoogleBookSearchViewableResource(ViewableResource resource, CustomAttachment attachment,
		SelectionService selection, SectionInfo info)
	{
		super(resource);
		this.googleBookAttachment = attachment;
		if( selection.getCurrentSession(info) != null )
		{
			resource.setAttribute(ViewableResource.PREFERRED_LINK_TARGET, "_blank");
		}
	}

	@Override
	public boolean hasContentStream()
	{
		return false;
	}

	@Override
	public String getMimeType()
	{
		return GoogleBookConstants.MIME_TYPE;
	}

	@Override
	public boolean isExternalResource()
	{
		return true;
	}

	@Override
	public Bookmark createCanonicalUrl()
	{
		return new SimpleBookmark(getBookUrl());
	}

	private String getBookUrl()
	{
		return (String) googleBookAttachment.getData(GoogleBookConstants.PROPERTY_URL);
	}

	@Override
	public ViewAuditEntry getViewAuditEntry()
	{
		return new ViewAuditEntry("googlebooks", getBookUrl());
	}

	private String getThumbUrl()
	{
		return (String) googleBookAttachment.getData(GoogleBookConstants.PROPERTY_THUMB_URL);
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
	public ImageRenderer createStandardThumbnailRenderer(Label label)
	{
		ImageRenderer thumb = new ImageRenderer(getThumbUrl(), label);
		// thumb.setWidth(googleBookSearchAttachment.getData("thumbWidth").toString());
		// thumb.setHeight(googleBookSearchAttachment.getData("thumbHeight").toString());
		return thumb;
	}

	@Override
	public boolean isCustomThumb()
	{
		return true;
	}

	@Override
	public List<AttachmentDetail> getCommonAttachmentDetails()
	{
		List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();

		// Type
		commonDetails.add(makeDetail(TYPE, MIMETYPE));

		// Name
		String name = googleBookAttachment.getDescription();
		if( !Check.isEmpty(name) )
		{
			commonDetails.add(makeDetail(TITLE, new TextLabel(name)));
		}

		// Published
		String date = (String) googleBookAttachment.getData(GoogleBookConstants.PROPERTY_PUBLISHED);
		if( date != null )
		{
			String[] split = date.split("-");
			commonDetails.add(makeDetail(PUBLISHED, new TextLabel(split[0])));
		}

		// Pages
		String pages = (String) googleBookAttachment.getData(GoogleBookConstants.PROPERTY_FORMATS);
		if( pages != null )
		{
			commonDetails.add(makeDetail(PAGES, new TextLabel(pages)));
		}

		return commonDetails;
	}
}
