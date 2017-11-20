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
