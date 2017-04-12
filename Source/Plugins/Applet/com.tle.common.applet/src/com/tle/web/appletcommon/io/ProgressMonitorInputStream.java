package com.tle.web.appletcommon.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressMonitorInputStream extends FilterInputStream
{
	private final ProgressMonitorCallback callback;

	public ProgressMonitorInputStream(InputStream delegate, ProgressMonitorCallback callback)
	{
		super(delegate);
		this.callback = callback;
	}

	@Override
	public int read() throws IOException
	{
		int result = super.read();
		if( result != -1 )
		{
			callback.addToProgress(1);
		}
		return result;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int result = super.read(b, off, len);
		if( result > 0 )
		{
			callback.addToProgress(result);
		}
		return result;
	}
}
