package com.tle.core.util.ims.extension;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.core.util.ims.beans.IMSManifest;

/**
 * @author Aaron
 */
public interface IMSManifestExporter
{
	void exportManifest(Item item, FileHandle root, IMSManifest manifest);

	void importManifest(Item item, FileHandle root, IMSManifest manifest);
}
