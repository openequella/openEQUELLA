package com.tle.common.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that stops when it's had enough. (ie. throws a
 * ByteLimitExceededException)
 * 
 * @author aholland
 */
public class ByteLimitedInputStream extends InputStream
{
	private final InputStream inner;
	private final long limit;
	private long counter;

	public ByteLimitedInputStream(InputStream inner, long limit)
	{
		this.inner = inner;
		this.limit = limit;
	}

	@Override
	public int read() throws IOException
	{
		incCounter();
		if( counter > limit )
		{
			throw new ByteLimitExceededException(counter, limit);
		}
		return inner.read();
	}

	protected void incCounter()
	{
		counter++;
	}

	protected long getCounter()
	{
		return counter;
	}

	@Override
	public long skip(long l) throws IOException
	{
		return inner.skip(l);
	}

	@Override
	public int available() throws IOException
	{
		return inner.available();
	}

	@Override
	public void close() throws IOException
	{
		inner.close();
	}

	@Override
	public synchronized void mark(int i)
	{
		inner.mark(i);
	}

	@Override
	public synchronized void reset() throws IOException
	{
		inner.reset();
	}

	@Override
	public boolean markSupported()
	{
		return inner.markSupported();
	}

	public static class ByteLimitExceededException extends IOException
	{
		private final long limit;
		private final long counter;

		public ByteLimitExceededException(long counter, long limit)
		{
			this.counter = counter;
			this.limit = limit;
		}

		public long getLimit()
		{
			return limit;
		}

		public long getCounter()
		{
			return counter;
		}
	}
}
