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
