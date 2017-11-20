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

import java.util.Date;

import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@SuppressWarnings("nls")
public class FlickrAttachmentBean extends EquellaAttachmentBean
{
	private String imageDimensions;
	private String author;
	private Date datePosted;
	private Date dateTaken;
	private String licenseName;
	private String viewUrl;
	private String thumbUrl;
	private String licenseCode;
	private String licenseKey;
	private String photoId;
	private String mediumUrl;

	public String getImageDimensions()
	{
		return imageDimensions;
	}

	public void setImageDimensions(String imageDimensions)
	{
		this.imageDimensions = imageDimensions;
	}

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public Date getDatePosted()
	{
		return datePosted;
	}

	public void setDatePosted(Date datePosted)
	{
		this.datePosted = datePosted;
	}

	public Date getDateTaken()
	{
		return dateTaken;
	}

	public void setDateTaken(Date dateTaken)
	{
		this.dateTaken = dateTaken;
	}

	public String getLicenseName()
	{
		return licenseName;
	}

	public void setLicenseName(String licenseName)
	{
		this.licenseName = licenseName;
	}

	public String getThumbUrl()
	{
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl)
	{
		this.thumbUrl = thumbUrl;
	}

	public String getViewUrl()
	{
		return viewUrl;
	}

	public void setViewUrl(String viewUrl)
	{
		this.viewUrl = viewUrl;
	}

	@Override
	public String getRawAttachmentType()
	{
		return "custom/flickr";
	}

	public String getLicenseCode()
	{
		return licenseCode;
	}

	public void setLicenseCode(String licenseCode)
	{
		this.licenseCode = licenseCode;
	}

	public String getLicenseKey()
	{
		return licenseKey;
	}

	public void setLicenseKey(String licenseKey)
	{
		this.licenseKey = licenseKey;
	}

	public String getPhotoId()
	{
		return photoId;
	}

	public void setPhotoId(String photoId)
	{
		this.photoId = photoId;
	}

	public String getMediumUrl()
	{
		return mediumUrl;
	}

	public void setMediumUrl(String mediumUrl)
	{
		this.mediumUrl = mediumUrl;
	}
}
