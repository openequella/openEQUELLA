package com.tle.web.controls.kaltura;

import java.util.Date;

import com.tle.common.kaltura.KalturaUtils;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor;

@Bind
public class KalturaAttachmentEditor extends AbstractCustomAttachmentEditor
{
	@Override
	public String getCustomType()
	{
		return KalturaUtils.ATTACHMENT_TYPE;
	}

	public void editKalturaServer(String serverUuid)
	{
		editCustomData(KalturaUtils.PROPERTY_KALTURA_SERVER, serverUuid);
	}

	public void editMediaId(String mediaId)
	{
		editCustomData(KalturaUtils.PROPERTY_ENTRY_ID, mediaId);
	}

	public void editTitle(String title)
	{
		editCustomData(KalturaUtils.PROPERTY_TITLE, title);
	}

	public void editUploadedDate(Date uploadedDate)
	{
		editCustomData(KalturaUtils.PROPERTY_DATE, uploadedDate.getTime());
	}

	public void editThumbUrl(String thumbUrl)
	{
		editCustomData(KalturaUtils.PROPERTY_THUMB_URL, thumbUrl);
	}

	public void editTags(String tags)
	{
		editCustomData(KalturaUtils.PROPERTY_TAGS, tags);
	}

	public void editDuration(long durationSeconds)
	{
		editCustomData(KalturaUtils.PROPERTY_DURATION, durationSeconds);
	}
}
