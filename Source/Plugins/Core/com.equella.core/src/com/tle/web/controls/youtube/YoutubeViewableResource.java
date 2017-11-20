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

package com.tle.web.controls.youtube;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
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

@SuppressWarnings("nls")
public class YoutubeViewableResource extends AbstractWrappedResource
{
	static
	{
		PluginResourceHandler.init(YoutubeViewableResource.class);
	}

	@PlugKey("youtube.details.type")
	private static KeyLabel TYPE;
	@PlugKey("youtube.details.mimetype")
	private static KeyLabel MIMETYPE;
	@PlugKey("youtube.details.title")
	private static KeyLabel NAME;
	@PlugKey("youtube.details.duration")
	private static KeyLabel DURATION;
	@PlugKey("youtube.details.author")
	private static KeyLabel AUTHOR;
	@PlugKey("youtube.details.uploaded")
	private static KeyLabel UPLOADED;
	@PlugKey("youtube.details.tags")
	private static KeyLabel TAGS;

	private final CustomAttachment youTubeAttachment;
	private DateRendererFactory dateRendererFactory;

	public YoutubeViewableResource(ViewableResource resource, CustomAttachment attachment, SelectionService selection,
		SectionInfo info, DateRendererFactory dateRendererFactory)
	{
		super(resource);
		this.youTubeAttachment = attachment;
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
		return YoutubeUtils.MIME_TYPE;
	}

	@Override
	public boolean isExternalResource()
	{
		return true;
	}

	@Override
	public Bookmark createCanonicalUrl()
	{
		return new SimpleBookmark(getPlayUrl());
	}

	private String getPlayUrl()
	{
		StringBuilder sb = new StringBuilder("//www.youtube.com/embed/");
		sb.append((String) youTubeAttachment.getData(YoutubeUtils.PROPERTY_ID));
		String extraParams = (String) youTubeAttachment.getData(YoutubeUtils.PROPERTY_PARAMETERS);
		if( !Check.isEmpty(extraParams) )
		{
			sb.append("?");
			Iterator<String> paramIterator = Arrays.asList(extraParams.split(",")).iterator();
			while( paramIterator.hasNext() )
			{
				String param = paramIterator.next();
				param = param.trim();
				param = param.replace(" ", "");
				sb.append(param);
				if( paramIterator.hasNext() )
				{
					sb.append("&");
				}
			}
		}
		return sb.toString();
	}

	@Override
	public ViewAuditEntry getViewAuditEntry()
	{
		return new ViewAuditEntry("youtube", getPlayUrl());
	}

	private String getThumbUrl()
	{
		return (String) youTubeAttachment.getData(YoutubeUtils.PROPERTY_THUMB_URL);
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
		return new ImageRenderer(getThumbUrl(), alt);
	}

	@Override
	public boolean isCustomThumb()
	{
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<AttachmentDetail> getCommonAttachmentDetails()
	{
		List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();

		// Type
		commonDetails.add(makeDetail(TYPE, MIMETYPE));

		// Name (Proper YouTube video title)
		String name = (String) youTubeAttachment.getData(YoutubeUtils.PROPERTY_TITLE);
		if( !Check.isEmpty(name) )
		{
			commonDetails.add(makeDetail(NAME, new TextLabel(name)));
		}

		// Duration
		Object durationData = youTubeAttachment.getData(YoutubeUtils.PROPERTY_DURATION);
		if( durationData instanceof String )
		{
			String duration = (String) durationData;
			setDuration(commonDetails, YoutubeUtils.formatDuration(duration));
		}
		else if( durationData instanceof Long )
		{
			long oldDuration = (long) durationData;
			setDuration(commonDetails, YoutubeUtils.formatDuration(oldDuration));
		}

		// Author
		String author = (String) youTubeAttachment.getData(YoutubeUtils.PROPERTY_AUTHOR);
		if( !Check.isEmpty(author) )
		{
			commonDetails.add(makeDetail(AUTHOR, new TextLabel(author)));
		}

		// Uploaded
		Long date = (Long) youTubeAttachment.getData(YoutubeUtils.PROPERTY_DATE);
		if( date != null )
		{
			commonDetails.add(makeDetail(UPLOADED, dateRendererFactory.createDateRenderer(new Date(date))));
		}

		// Tags
		String tags = (String) youTubeAttachment.getData(YoutubeUtils.PROPERTY_TAGS);
		if( !Check.isEmpty(tags) )
		{
			commonDetails.add(makeDetail(TAGS, new TextLabel(tags)));
		}

		return commonDetails;
	}

	private void setDuration(List<AttachmentDetail> commonDetails, String duration)
	{
		if( duration != null )
		{
			commonDetails.add(makeDetail(DURATION, new TextLabel(duration)));
		}
	}
}