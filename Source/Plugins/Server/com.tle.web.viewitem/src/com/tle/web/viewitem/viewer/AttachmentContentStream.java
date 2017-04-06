package com.tle.web.viewitem.viewer;

import com.tle.web.stream.ContentStream;
import com.tle.web.stream.WrappedContentStream;

@SuppressWarnings("nls")
public class AttachmentContentStream extends WrappedContentStream
{
	public AttachmentContentStream(ContentStream inner)
	{
		super(inner);
	}

	@Override
	public String getContentDisposition()
	{
		return "attachment";
	}
}