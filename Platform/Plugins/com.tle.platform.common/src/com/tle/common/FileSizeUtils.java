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

package com.tle.common;

import java.text.DecimalFormat;

import com.dytech.common.GeneralConstants;

public class FileSizeUtils
{
	private static DecimalFormat SIZE_FORMAT = new DecimalFormat("#.##"); //$NON-NLS-1$

	@SuppressWarnings("nls")
	public static String humanReadableFileSize(long size)
	{
		String fsize;
		if( size < GeneralConstants.BYTES_PER_KILOBYTE )
		{
			fsize = size + " bytes";
		}
		else if( size < GeneralConstants.BYTES_PER_MEGABYTE )
		{
			fsize = SIZE_FORMAT.format(size / (float) GeneralConstants.BYTES_PER_KILOBYTE) + " KB";
		}
		else
		{
			fsize = SIZE_FORMAT.format(size / (float) GeneralConstants.BYTES_PER_MEGABYTE) + " MB";
		}
		return fsize;
	}

	@SuppressWarnings("nls")
	public static String humanReadableGigabyte(double size)
	{
		return SIZE_FORMAT.format(size / GeneralConstants.GIGABYTE) + " GB";
	}
}
