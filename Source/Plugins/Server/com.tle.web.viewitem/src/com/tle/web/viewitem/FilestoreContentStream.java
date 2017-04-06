package com.tle.web.viewitem;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.ItemKey;
import com.tle.web.stream.ContentStream;

public interface FilestoreContentStream extends ContentStream
{
	ItemKey getItemId();

	FileHandle getFileHandle();

	String getFileDirectoryPath();

	String getFilepath();
}