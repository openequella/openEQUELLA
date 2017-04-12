package com.tle.core.filesystem;

/**
 * @author Aaron
 */
public class BucketFile extends AbstractFile implements TemporaryFileHandle
{
	private static final long serialVersionUID = 1L;

	public BucketFile(TemporaryFileHandle parent, Object idForBucketingHash)
	{
		super(parent, FileHandleUtils.getBucketFolder(idForBucketingHash));
	}
}
