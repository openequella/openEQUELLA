/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
