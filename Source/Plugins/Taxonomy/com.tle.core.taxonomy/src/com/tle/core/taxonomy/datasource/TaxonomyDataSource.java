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

package com.tle.core.taxonomy.datasource;

import java.util.List;
import java.util.Map;

import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.Pair;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.core.taxonomy.TermResult;

public interface TaxonomyDataSource
{
	TermResult getTerm(String fullTermPath);

	/**
	 * @param parentFullTermPath use null or empty string to retrieve root
	 *            terms.
	 */
	List<TermResult> getChildTerms(String parentFullTermPath);

	/**
	 * @param query
	 * @param restriction
	 * @param limit
	 * @return The first value is the total available results, the second is the
	 *         returned results which may be limited by limit
	 */
	Pair<Long, List<TermResult>> searchTerms(String query, SelectionRestriction restriction, int limit,
		boolean searchFullTerm);

	/**
	 * Retrieve custom data from a taxonomy node.
	 * 
	 * @param fullTermPath
	 * @param key
	 * @return
	 */
	String getDataForTerm(String fullTermPath, String key);

	/**
	 * Adds a new term into the taxonomy. Is intended to only be used for the
	 * Auto-Complete edit box. For full-blown term creation, see insertTerm on
	 * RemoteTermService.
	 * 
	 * @param parentFullTermPath The full path of the parent term the new term
	 *            will be added to, eg, Mammalia/Felidae/Panthera.
	 * @param termValue The term to be added, eg, Tiger.
	 */
	TermResult addTerm(String parentFullTermPath, String termValue, boolean createHierarchy);

	void validateTerm(String parentFullTermPath, String termValue, boolean requireParent) throws InvalidDataException;

	boolean supportsTermAddition();

	boolean supportsTermBrowsing();

	boolean supportsTermSearching();

	/**
	 * Is the data source readonly
	 * 
	 * @return boolean
	 */
	boolean isReadonly();

	/**
	 * Get term by uuid
	 * 
	 * @param termUuid
	 * @return
	 */
	TermResult getTermByUuid(String termUuid);

	/**
	 * Get a data value for a key against a term
	 * 
	 * @param taxonomy
	 * @param termUuid
	 * @param dataKey
	 * @return String
	 */
	String getDataByTermUuid(String termUuid, String dataKey);

	/**
	 * Get all data
	 * 
	 * @param termUuid
	 * @return
	 */
	Map<String, String> getAllDataByTermUuid(String termUuid);
}
