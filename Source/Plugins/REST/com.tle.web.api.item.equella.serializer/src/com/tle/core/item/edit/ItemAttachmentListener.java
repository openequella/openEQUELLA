package com.tle.core.item.edit;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;

public interface ItemAttachmentListener
{
	void attachmentsChanged(ItemEditor editor, Item item, FileHandle fileHandle);
}
