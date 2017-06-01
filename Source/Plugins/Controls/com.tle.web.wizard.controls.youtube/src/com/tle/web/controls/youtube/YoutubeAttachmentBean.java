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

import java.util.Date;

import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@SuppressWarnings("nls")
public class YoutubeAttachmentBean extends EquellaAttachmentBean
{
	private String videoId;
	private String title;
	private String uploader;
	private Date uploadedDate;
	private String viewUrl;
	private String thumbUrl;
	private String tags;
	private String duration;
	private String customParameters;

	public String getVideoId()
	{
		return videoId;
	}

	public void setVideoId(String videoId)
	{
		this.videoId = videoId;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getUploader()
	{
		return uploader;
	}

	public void setUploader(String uploader)
	{
		this.uploader = uploader;
	}

	public String getViewUrl()
	{
		return viewUrl;
	}

	public void setViewUrl(String viewUrl)
	{
		this.viewUrl = viewUrl;
	}

	public String getThumbUrl()
	{
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl)
	{
		this.thumbUrl = thumbUrl;
	}

	public String getTags()
	{
		return tags;
	}

	public void setTags(String tags)
	{
		this.tags = tags;
	}

	public Date getUploadedDate()
	{
		return uploadedDate;
	}

	public void setUploadedDate(Date uploadedDate)
	{
		this.uploadedDate = uploadedDate;
	}

	public String getDuration()
	{
		return duration;
	}

	public void setDuration(String duration)
	{
		this.duration = duration;
	}

	@Override
	public String getRawAttachmentType()
	{
		return "custom/youtube";
	}

	public String getCustomParameters()
	{
		return customParameters;
	}

	public void setCustomParameters(String customParameters)
	{
		this.customParameters = customParameters;
	}
}
