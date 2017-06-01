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

package com.tle.web.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayContentStream extends AbstractContentStream
{
	private final byte[] bytes;
	private final ByteArrayInputStream inp;

	public ByteArrayContentStream(byte[] bytes, String filename, String mimeType)
	{
		super(filename, mimeType);
		inp = new ByteArrayInputStream(bytes);
		this.bytes = bytes;
	}

	@Override
	public long getContentLength()
	{
		return bytes.length;
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return inp;
	}
}
