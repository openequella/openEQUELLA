package com.tle.core.institution.convert.importhandler;

import java.util.Iterator;

/**
 * Interface for different importer implementations. Older versions of 3.1 and
 * 3.2 would generate exports with a single file that contained all the nodes
 * (see SingleFileImportHandler), while the new format separates these out
 * sequentially numbered files.
 */
public interface ImportHandler<NODE_TYPE>
{
	int getNodeCount();

	Iterator<NODE_TYPE> iterateNodes();
}