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

package com.tle.web.controls.flickr;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Throwables;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.Check;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.render.DateRendererFactory;
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

/**
 * @author Larry. Based on the YouTube plugin.
 */
@SuppressWarnings("nls")
public class FlickrViewableResource extends AbstractWrappedResource
{
	static
	{
		PluginResourceHandler.init(FlickrViewableResource.class);
	}

	@PlugKey("flickr.details.type")
	private static KeyLabel TYPE;
	@PlugKey("flickr.details.mimetype.description")
	private static KeyLabel MIMETYPE;
	@PlugKey("flickr.details.title")
	private static KeyLabel NAME;
	@PlugKey("flickr.details.imagesize")
	private static KeyLabel FULL_IMAGE_SIZE;
	@PlugKey("flickr.details.author")
	private static KeyLabel AUTHOR;
	@PlugKey("flickr.details.uploaded")
	private static KeyLabel UPLOADED;
	@PlugKey("flickr.details.taken")
	private static KeyLabel TAKEN;
	@PlugKey("flickr.details.licencename")
	private static KeyLabel LICENCE_NAME;

	private final CustomAttachment flickrAttachment;
	private DateRendererFactory dateRendererFactory;

	public FlickrViewableResource(ViewableResource resource, CustomAttachment attachment, SelectionService selection,
		SectionInfo info, DateRendererFactory dateRendererFactory)
	{
		super(resource);
		this.flickrAttachment = attachment;
		this.dateRendererFactory = dateRendererFactory;
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
		return FlickrUtils.MIME_TYPE;
	}

	@Override
	public Bookmark createCanonicalUrl()
	{
		return new SimpleBookmark(getPlayUrl());
	}

	@Override
	public boolean isExternalResource()
	{
		return true;
	}

	private String getPlayUrl()
	{
		return (String) flickrAttachment.getData(FlickrUtils.PROPERTY_SHOW_URL);
	}

	@Override
	public ViewAuditEntry getViewAuditEntry()
	{
		return new ViewAuditEntry("flickr", getPlayUrl());
	}

	private String getThumbUrl()
	{
		return (String) flickrAttachment.getData(FlickrUtils.PROPERTY_THUMB_URL);
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
	public ImageRenderer createStandardThumbnailRenderer(Label alt)
	{
		ImageRenderer thumb = new ImageRenderer(getThumbUrl(), alt);
		// thumb.setWidth(flickrAttachment.getData("thumbWidth").toString());
		// thumb.setHeight(flickrAttachment.getData("thumbHeight").toString());
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
		String name = flickrAttachment.getDescription();
		if( !Check.isEmpty(name) )
		{
			commonDetails.add(makeDetail(NAME, new TextLabel(name)));
		}

		// Dimensions
		String dimensions = (String) flickrAttachment.getData(FlickrUtils.PROPERTY_IMAGE_SIZE);
		if( !Check.isEmpty(dimensions) )
		{
			commonDetails.add(makeDetail(FULL_IMAGE_SIZE, new TextLabel(dimensions)));
		}

		// Author
		String author = (String) flickrAttachment.getData(FlickrUtils.PROPERTY_AUTHOR);
		if( !Check.isEmpty(author) )
		{
			commonDetails.add(makeDetail(AUTHOR, new TextLabel(author)));
		}

		// Uploaded
		Date datePosted = (Date) flickrAttachment.getData(FlickrUtils.PROPERTY_DATE_POSTED);
		if( datePosted != null )
		{
			commonDetails.add(makeDetail(UPLOADED, dateRendererFactory.createDateRenderer(datePosted)));
		}

		// Taken
		Date dateTaken = (Date) flickrAttachment.getData(FlickrUtils.PROPERTY_DATE_TAKEN);
		if( dateTaken != null )
		{
			commonDetails.add(makeDetail(TAKEN, dateRendererFactory.createDateRenderer(dateTaken)));
		}

		// Licence Name
		String licenceName = (String) flickrAttachment.getData(FlickrUtils.PROPERTY_LICENCE_NAME);
		if( !Check.isEmpty(licenceName) )
		{
			commonDetails.add(makeDetail(LICENCE_NAME, new TextLabel(licenceName)));
		}

		return commonDetails;
	}
}