package com.tle.web.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayContentStream extends AbstractContentStream
{
	private final byte[] bytes;
	private final ByteArrayInputStream inp;

	public ByteArrayContentStream(byte[] bytes, String filename, String mimeType)
	{
		super(filename, mimeType);
		inp = new ByteArrayInputStream(bytes);
		this.bytes = bytes;
	}

	@Override
	public long getContentLength()
	{
		return bytes.length;
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return inp;
	}
}
