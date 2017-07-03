package com.tle.common.filesystem.handle;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.filesystem.FileHandleUtils;

/**
 * @author Aaron
 */
@NonNullByDefault
public class BucketFile extends AbstractFile implements TemporaryFileHandle
{
	private static final long serialVersionUID = 1L;

	public BucketFile(TemporaryFileHandle parent, Object idForBucketingHash)
	{
		super(parent, FileHandleUtils.getBucketFolder(idForBucketingHash));
	}
}
