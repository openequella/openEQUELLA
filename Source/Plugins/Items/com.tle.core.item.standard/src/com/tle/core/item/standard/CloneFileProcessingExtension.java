package com.tle.core.item.standard;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;

/**
 * @author Aaron
 */
public interface CloneFileProcessingExtension
{
	void processFiles(ItemId oldId, FileHandle oldHandle, Item newItem, FileHandle newHandle);
}
