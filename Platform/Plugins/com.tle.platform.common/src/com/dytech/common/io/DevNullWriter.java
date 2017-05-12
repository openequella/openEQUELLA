/*
 * Created on Sep 12, 2005
 */
package com.dytech.common.io;

import java.io.Writer;

/**
 * @author Nicholas Read
 */
public class DevNullWriter extends Writer
{
	public DevNullWriter()
	{
		super();
	}

	@Override
	public void write(int c)
	{
		// We don't care if they are trying to write to us.
	}

	@Override
	public void write(char[] cbuf)
	{
		// We don't care if they are trying to write to us.
	}

	@Override
	public void write(char[] cbuf, int off, int len)
	{
		// We don't care if they are trying to write to us.
	}

	@Override
	public void write(String str)
	{
		// We don't care if they are trying to write to us.
	}

	@Override
	public void write(String str, int off, int len)
	{
		// We don't care if they are trying to write to us.
	}

	@Override
	public void flush()
	{
		// There is nothing to flush!
	}

	@Override
	public void close()
	{
		// There is nothing to close!
	}
}
