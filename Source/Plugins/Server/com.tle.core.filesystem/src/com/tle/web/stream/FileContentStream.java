package com.tle.web.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileContentStream extends AbstractContentStream
{
	private File file;
	private FileInputStream inp;

	public FileContentStream(File file, String filename, String mimeType)
	{
		super(filename, mimeType);
		this.file = file;
	}

	@Override
	public boolean exists()
	{
		return file != null && file.exists() && !file.isDirectory();
	}

	@Override
	public long getContentLength()
	{
		return file.length();
	}

	@Override
	public File getDirectFile()
	{
		return file;
	}

	@Override
	public long getLastModified()
	{
		return file.lastModified();
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		if( inp == null )
		{
			inp = new FileInputStream(file);
		}
		return inp;
	}

}
