package com.tle.web.controls.itunesu;

import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor;

@Bind
public class ITunesUAttachmentEditor extends AbstractCustomAttachmentEditor
{

	@Override
	public String getCustomType()
	{
		return ITunesUHandler.ITUNESU_TYPE;
	}

	public void editPlayUrl(String playUrl)
	{
		editCustomData(ITunesUHandler.ITUNESU_URL, playUrl);
	}

	public void editTrackName(String trackName)
	{
		editCustomData(ITunesUHandler.ITUNESU_TRACK, trackName);
	}

}
