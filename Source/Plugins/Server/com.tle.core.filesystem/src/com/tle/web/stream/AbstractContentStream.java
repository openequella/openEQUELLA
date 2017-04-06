package com.tle.web.stream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractContentStream implements ContentStream
{
	protected String contentDisposition;
	protected String cacheControl;
	protected String mimeType;
	protected String filenameWithoutPath;

	public AbstractContentStream(String filenameWithoutPath, String mimeType)
	{
		this.filenameWithoutPath = filenameWithoutPath;
		this.mimeType = mimeType;
	}

	@Override
	public String getContentDisposition()
	{
		return contentDisposition;
	}

	@Override
	public String getCacheControl()
	{
		return cacheControl;
	}

	public void setContentDisposition(String contentDisposition)
	{
		this.contentDisposition = contentDisposition;
	}

	@Override
	public void setCacheControl(String cacheControl)
	{
		this.cacheControl = cacheControl;
	}

	@Override
	public String getMimeType()
	{
		return mimeType;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

	@Override
	public void write(OutputStream out) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean mustWrite()
	{
		return false;
	}

	@Override
	public File getDirectFile()
	{
		return null;
	}

	@Override
	public long getLastModified()
	{
		return -1;
	}

	@Override
	public String getFilenameWithoutPath()
	{
		return filenameWithoutPath;
	}

	@Override
	public boolean exists()
	{
		return true;
	}

	@Override
	public long getEstimatedContentLength()
	{
		return getContentLength();
	}

	public void setFilenameWithoutPath(String filenameWithoutPath)
	{
		this.filenameWithoutPath = filenameWithoutPath;
	}
}
