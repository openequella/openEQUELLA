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
