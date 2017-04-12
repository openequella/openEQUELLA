/*
 * Created on 4/11/2005
 */
package com.tle.common.adminconsole;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.tle.beans.item.Item;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;

public interface RemoteAdminService
{
	/**
	 * @return a map of allowed tool extension IDs to the set of matching
	 *         privileges for that tool.
	 */
	Map<String, Set<String>> getAllowedTools();

	void reindexSearchEngine();

	void uploadFile(String staging, String filename, byte[] bytes, boolean append) throws IOException;

	byte[] downloadFile(String staging, String filename) throws IOException;

	void removeFile(String staging, String filename);

	void clearStaging(String staging);

	String createStaging();

	SearchResults<Item> searchReducedItems(Search search, int start, int count);
}
