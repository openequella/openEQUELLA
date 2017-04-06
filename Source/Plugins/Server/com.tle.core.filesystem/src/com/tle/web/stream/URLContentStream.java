package com.tle.web.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class URLContentStream extends AbstractContentStream
{
	private URLConnection urlConnection;

	public URLContentStream(URL url, String filepath, String mimeType) throws IOException
	{
		super(filepath, mimeType);
		urlConnection = url.openConnection();
	}

	@Override
	public boolean exists()
	{
		try
		{
			urlConnection.connect();
			return true;
		}
		catch( IOException e )
		{
			return false;
		}
	}

	@Override
	public long getContentLength()
	{
		return urlConnection.getContentLength();
	}

	@Override
	public long getLastModified()
	{
		return urlConnection.getLastModified();
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return urlConnection.getInputStream();
	}

}
