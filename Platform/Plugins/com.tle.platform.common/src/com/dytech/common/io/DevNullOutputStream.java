/*
 * Created on Sep 12, 2005
 */
package com.dytech.common.io;

import java.io.OutputStream;

/**
 * @author Nicholas Read
 */
@Deprecated
public class DevNullOutputStream extends OutputStream
{
	public DevNullOutputStream()
	{
		super();
	}

	@Override
	public void write(int b)
	{
		// We don't care if they are trying to write to us.
	}

	@Override
	public void write(byte[] b)
	{
		// We don't care if they are trying to write to us.
	}

	@Override
	public void write(byte[] b, int off, int len)
	{
		// We don't care if they are trying to write to us.
	}
}
