package com.tle.common.beans;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.tle.common.beans.progress.PercentageProgressCallback;

/**
 * @author Nicholas Read
 */
public class UploadCallbackInputStream extends FilterInputStream
{
	private final PercentageProgressCallback callback;

	public UploadCallbackInputStream(InputStream in, PercentageProgressCallback callback)
	{
		super(in);

		this.callback = callback;
	}

	@Override
	public int read() throws IOException
	{
		int abyte = super.read();
		if( abyte == -1 )
		{
			callback.setFinished();
			return -1;
		}
		callback.incrementBytesRead(1);
		return abyte;

	}

	@Override
	public int read(byte[] b) throws IOException
	{
		int count = super.read(b);
		if( count == -1 )
		{
			callback.setFinished();
			return -1;
		}
		callback.incrementBytesRead(count);
		return count;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int count = super.read(b, off, len);
		if( count == -1 )
		{
			callback.setFinished();
			return -1;
		}
		callback.incrementBytesRead(count);
		return count;
	}
}
