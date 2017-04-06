package com.tle.web.echo.data;

import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor;
import com.tle.web.echo.EchoUtils;

@Bind
public class EchoAttachmentEditor extends AbstractCustomAttachmentEditor
{
	@Override
	public String getCustomType()
	{
		return EchoUtils.ATTACHMENT_TYPE;
	}

	public void editEchoData(String data)
	{
		editCustomData(EchoUtils.PROPERTY_ECHO_DATA, data);
	}
}