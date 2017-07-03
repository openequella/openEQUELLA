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

package com.tle.core.remoterepo.z3950.service;

import java.util.List;

import com.tle.beans.entity.FederatedSearch;
import com.tle.common.NameValue;
import com.tle.common.searching.SearchResults;
import com.tle.core.fedsearch.GenericRecord;
import com.tle.core.i18n.BundleCache;
import com.tle.core.remoterepo.z3950.AdvancedSearchOptions;
import com.tle.core.remoterepo.z3950.Z3950SearchResult;
import com.tle.core.remoting.RemoteZ3950Service;

/**
 * @author aholland
 */
public interface Z3950Service extends RemoteZ3950Service
{
	SearchResults<Z3950SearchResult> search(FederatedSearch z3950Search, String query, int offset, int perpage,
		AdvancedSearchOptions advanced);

	/**
	 * Bit dodge. Uses the index which could change in between search and record
	 * retrieval.
	 * 
	 * @param z3950Search
	 * @param qs
	 * @param perpage
	 * @param index
	 * @return
	 */
	GenericRecord getRecord(FederatedSearch z3950Search, String qs, int index, AdvancedSearchOptions advanced,
		boolean useImportSchema);

	List<NameValue> convertAdvancedFieldsXml(String xml, BundleCache cache);
}
