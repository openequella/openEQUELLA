package com.tle.core.util.ims;

import java.util.Collection;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.core.util.ims.beans.IMSManifest;

public interface IMSNavigationHelper
{
	/**
	 * @param manifest
	 * @param item
	 * @param root
	 * @param packageFolder
	 * @param isScorm
	 * @return A collection of created attachments
	 */
	Collection<Attachment> createTree(IMSManifest manifest, Item item, FileHandle root, String packageFolder,
		boolean scorm, boolean expand);
}
