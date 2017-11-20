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

package com.tle.common.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.common.PathUtils;

@NonNullByDefault
public final class FileHandleUtils
{
	private static final int NUMBER_OF_FOLDERS = 128;

	public static String getBucketFolder(Object id)
	{
		final int folder = id.hashCode() & (NUMBER_OF_FOLDERS - 1);
		return Integer.toString(folder);
	}

	public static String getHashedPath(Object id)
	{
		return PathUtils.filePath(getBucketFolder(id), id.toString());
	}

	@SuppressWarnings("nls")
	public static void checkPath(String path)
	{
		Check.checkNotEmpty(path);

		if( path.indexOf("..") >= 0 )
		{
			throw new IllegalArgumentException("Argument must not contain parent reference");
		}
	}

	private FileHandleUtils()
	{
		throw new Error();
	}
}
