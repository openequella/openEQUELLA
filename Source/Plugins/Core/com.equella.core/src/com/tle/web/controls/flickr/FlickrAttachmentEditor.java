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

import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor;

@Bind
public class FlickrAttachmentEditor extends AbstractCustomAttachmentEditor
{
	@Override
	public String getCustomType()
	{
		return FlickrUtils.ATTACHMENT_TYPE;
	}

	public void editImageDimensions(String imageDimensions)
	{
		editCustomData(FlickrUtils.PROPERTY_IMAGE_SIZE, imageDimensions);
	}

	public void editAuthor(String author)
	{
		editCustomData(FlickrUtils.PROPERTY_AUTHOR, author);
	}

	public void editDatePosted(Date datePosted)
	{
		editCustomData(FlickrUtils.PROPERTY_DATE_POSTED, datePosted);
	}

	public void editDateTaken(Date dateTaken)
	{
		editCustomData(FlickrUtils.PROPERTY_DATE_TAKEN, dateTaken);
	}

	public void editLicenseName(String licenseName)
	{
		editCustomData(FlickrUtils.PROPERTY_LICENCE_NAME, licenseName);
	}

	public void editViewUrl(String viewUrl)
	{
		editCustomData(FlickrUtils.PROPERTY_SHOW_URL, viewUrl);
	}

	public void editThumbUrl(String thumbUrl)
	{
		editCustomData(FlickrUtils.PROPERTY_THUMB_URL, thumbUrl);
	}

	public void editLicenseCode(String licenseCode)
	{
		editCustomData(FlickrUtils.PROPERTY_LICENCE_CODE, licenseCode);
	}

	public void editLicenseKey(String licenseKey)
	{
		editCustomData(FlickrUtils.PROPERTY_LICENCE_KEY, licenseKey);
	}

	public void editPhotoId(String photoId)
	{
		editCustomData(FlickrUtils.PROPERTY_ID, photoId);
	}

	public void editMediumUrl(String mediumUrl)
	{
		editCustomData(FlickrUtils.PROPERTY_MEDIUM_URL, mediumUrl);
	}

}
