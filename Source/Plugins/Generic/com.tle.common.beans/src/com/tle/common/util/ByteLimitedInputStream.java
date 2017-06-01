/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
