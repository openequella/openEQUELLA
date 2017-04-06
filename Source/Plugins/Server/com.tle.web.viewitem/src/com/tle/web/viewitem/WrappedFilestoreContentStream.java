package com.tle.web.viewitem;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.ItemKey;
import com.tle.web.stream.WrappedContentStream;

public abstract class WrappedFilestoreContentStream extends WrappedContentStream implements FilestoreContentStream
{
	protected FilestoreContentStream innerFile;

	public WrappedFilestoreContentStream(FilestoreContentStream inner)
	{
		super(inner);
		this.innerFile = inner;
	}

	@Override
	public String getFileDirectoryPath()
	{
		return innerFile.getFileDirectoryPath();
	}

	@Override
	public FileHandle getFileHandle()
	{
		return innerFile.getFileHandle();
	}

	@Override
	public String getFilepath()
	{
		return innerFile.getFilepath();
	}

	@Override
	public ItemKey getItemId()
	{
		return innerFile.getItemId();
	}

}
