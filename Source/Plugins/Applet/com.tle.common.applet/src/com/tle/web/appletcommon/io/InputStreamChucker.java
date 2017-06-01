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

package com.tle.web.appletcommon.io;

import java.io.IOException;
import java.io.InputStream;

import com.dytech.common.GeneralConstants;

public final class InputStreamChucker
{
	public static void chunk(long length, InputStream content, ChunkHandler handler)
	{
		// #849 - we can't create too many connections when uploading large
		// files.
		// Make the byte array proportional to the file length.
		byte[] bytes = new byte[Math.max((int) (length / 100), 32 * GeneralConstants.BYTES_PER_KILOBYTE)];

		try
		{
			boolean append = false;
			int count = content.read(bytes);
			while( count >= 0 )
			{
				byte[] upload = bytes;
				if( count < bytes.length )
				{
					upload = new byte[count];
					System.arraycopy(bytes, 0, upload, 0, count);
				}

				handler.handleChunk(upload, append);

				append = true;
				count = content.read(bytes);
			}
		}
		catch( IOException ex )
		{
			throw new RuntimeException("Error", ex); //$NON-NLS-1$
		}
	}

	public interface ChunkHandler
	{
		void handleChunk(byte[] bytes, boolean append) throws IOException;
	}

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	private InputStreamChucker()
	{
		throw new Error();
	}
}
