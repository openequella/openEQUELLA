package com.tle.web.appletcommon.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ProgressMonitorOutputStream extends BufferedOutputStream
{
	private final ProgressMonitorCallback callback;

	public ProgressMonitorOutputStream(OutputStream delegate, ProgressMonitorCallback callback)
	{
		super(delegate);
		this.callback = callback;
	}

	/**
	 * For large files, it's disastrous to call the FilterOutputStream's
	 * implementations of write(...) - which results in calls the callback for
	 * every byte written to the output stream. Accordingly, class inheritance
	 * altered to bypass FilterOutputStream. See Redmine #6092.
	 */
	@Override
	public synchronized void write(int b) throws IOException
	{
		super.write(b);
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException
	{
		super.write(b, off, len);
		if( len > 0 && callback != null )
		{
			callback.addToProgress(len);
		}
	}
}