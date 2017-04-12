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
