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
