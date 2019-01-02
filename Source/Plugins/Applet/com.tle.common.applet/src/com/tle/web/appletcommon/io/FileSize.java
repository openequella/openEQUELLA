/*
 * Copyright 2019 Apereo
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

import java.io.File;
import java.util.List;

public final class FileSize
{
	public static long getFileSize(List<File> files)
	{
		return getFileSize(files.toArray(new File[files.size()]));
	}

	public static long getFileSize(File... files)
	{
		long total = 0;
		for( File file : files )
		{
			total += file.isDirectory() ? getFileSize(file.listFiles()) : file.length();
		}
		return total;
	}

	private FileSize()
	{
		throw new Error();
	}
}
