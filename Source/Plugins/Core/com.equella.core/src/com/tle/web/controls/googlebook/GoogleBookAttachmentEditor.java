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

import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor;

@Bind
public class GoogleBookAttachmentEditor extends AbstractCustomAttachmentEditor
{

	@Override
	public String getCustomType()
	{
		return GoogleBookConstants.ATTACHMENT_TYPE;
	}

	public void editBookId(String bookId)
	{
		editCustomData(GoogleBookConstants.PROPERTY_ID, bookId);
	}

	public void editViewUrl(String viewUrl)
	{
		editCustomData(GoogleBookConstants.PROPERTY_URL, viewUrl);
	}

	public void editThumbUrl(String thumbUrl)
	{
		editCustomData(GoogleBookConstants.PROPERTY_THUMB_URL, thumbUrl);
	}

	public void editPublishedDate(String publishedDate)
	{
		editCustomData(GoogleBookConstants.PROPERTY_PUBLISHED, publishedDate);
	}

	public void editPages(String pages)
	{
		editCustomData(GoogleBookConstants.PROPERTY_FORMATS, pages);
	}
}
