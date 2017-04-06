package com.tle.web.controls.youtube;

import java.util.Date;

import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor;

@Bind
public class YoutubeAttachmentEditor extends AbstractCustomAttachmentEditor
{
	@Override
	public String getCustomType()
	{
		return YoutubeUtils.ATTACHMENT_TYPE;
	}

	public void editVideoId(String videoId)
	{
		editCustomData(YoutubeUtils.PROPERTY_ID, videoId);
	}

	public void editTitle(String title)
	{
		editCustomData(YoutubeUtils.PROPERTY_TITLE, title);
	}

	public void editUploader(String uploader)
	{
		editCustomData(YoutubeUtils.PROPERTY_AUTHOR, uploader);
	}

	public void editUploadedDate(Date uploadedDate)
	{
		editCustomData(YoutubeUtils.PROPERTY_DATE, uploadedDate.getTime());
	}

	public void editDuration(String duration)
	{
		editCustomData(YoutubeUtils.PROPERTY_DURATION, duration);
	}

	public void editViewUrl(String viewUrl)
	{
		editCustomData(YoutubeUtils.PROPERTY_PLAY_URL, viewUrl);
	}

	public void editThumbUrl(String thumbUrl)
	{
		editCustomData(YoutubeUtils.PROPERTY_THUMB_URL, thumbUrl);
	}

	public void editTags(String tags)
	{
		editCustomData(YoutubeUtils.PROPERTY_TAGS, tags);
	}

	public void editCustomParameters(String params)
	{
		editCustomData(YoutubeUtils.PROPERTY_PARAMETERS, params);
	}

}
