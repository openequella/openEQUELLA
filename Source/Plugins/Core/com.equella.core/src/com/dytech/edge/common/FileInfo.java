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

package com.dytech.edge.common;

import java.io.Serializable;

/**
 * Bean for holding information about a file.
 */
public class FileInfo implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final long length;
	private final String filename;
	private final String md5CheckSum;

	public FileInfo(final long length, final String filename)
	{
		this(length, filename, null);
	}

	public FileInfo(final long length, final String filename, final String md5CheckSum)
	{
		this.length = length;
		this.filename = filename;
		this.md5CheckSum = md5CheckSum;
	}

	/**
	 * @return Returns the length.
	 */
	public long getLength()
	{
		return length;
	}

	/**
	 * @return Returns the filename (which may be different to the filename
	 *         requested)
	 */
	public String getFilename()
	{
		return filename;
	}

	/**
	 * @return Returns the md5CheckSum
	 */
	public String getMd5CheckSum()
	{
		return md5CheckSum;
	}
}
