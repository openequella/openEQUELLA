package com.tle.web.stream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class WrappedContentStream implements ContentStream
{
	protected ContentStream inner;

	public WrappedContentStream(ContentStream inner)
	{
		this.inner = inner;
	}

	@Override
	public boolean exists()
	{
		return inner.exists();
	}

	@Override
	public String getContentDisposition()
	{
		return inner.getContentDisposition();
	}

	@Override
	public long getContentLength()
	{
		return inner.getContentLength();
	}

	@Override
	public long getEstimatedContentLength()
	{
		return getContentLength();
	}

	@Override
	public File getDirectFile()
	{
		return inner.getDirectFile();
	}

	@Override
	public String getFilenameWithoutPath()
	{
		return inner.getFilenameWithoutPath();
	}

	@Override
	public long getLastModified()
	{
		return inner.getLastModified();
	}

	@Override
	public String getMimeType()
	{
		return inner.getMimeType();
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return inner.getInputStream();
	}

	@Override
	public String getCacheControl()
	{
		return inner.getCacheControl();
	}

	@Override
	public void setCacheControl(String cacheControl)
	{
		inner.setCacheControl(cacheControl);
	}

	@Override
	public boolean mustWrite()
	{
		return inner.mustWrite();
	}

	@Override
	public void write(OutputStream out) throws IOException
	{
		inner.write(out);
	}
}
