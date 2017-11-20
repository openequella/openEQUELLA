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

package com.tle.core.taxonomy;

import java.util.List;

import com.tle.common.beans.exception.InvalidDataException;
import com.tle.beans.Institution;
import com.tle.common.Pair;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.terms.RemoteTermService;
import com.tle.core.institution.convert.ConverterParams;

public interface TermService extends RemoteTermService
{
	/**
	 * Get TermResult by full term path
	 * 
	 * @param taxonomy
	 * @param fullTermPath
	 * @return
	 */
	TermResult getTermResult(Taxonomy taxonomy, String fullTermPath);

	List<TermResult> listTermResults(Taxonomy taxonomy, String parentFullPath);

	Pair<Long, List<TermResult>> searchTerms(Taxonomy taxonomy, String query, SelectionRestriction restriction,
		int limit, boolean searchFullTerms);

	/**
	 * Adds a new term to the taxonomy bypassing the EDIT_TAXONOMY security
	 * checks. This is intended to be used by the Auto-Complete edit box for
	 * folksonomies where users should be able to add terms, but not edit the
	 * taxonomy as a whole. Assumes that checks have already been made as to
	 * whether calling this is really allowed.
	 */
	TermResult addTerm(Taxonomy taxonomy, String parentFullPath, String termValue, boolean createHierarchy);

	void move(Taxonomy taxonomy, TermResult termToMove, TermResult parentTerm, int index);

	TermResult insertTerm(Taxonomy taxonomy, TermResult parentTerm, String termValue, int index);

	/**
	 * Make sure you put the @Transactional annotation on the implementation!
	 */
	void doInTransaction(Runnable runnable);

	void deleteForTaxonomy(Taxonomy taxonomy);

	void doExport(Taxonomy taxonomy, SubTemporaryFile dataSourceFolder, Institution institution, ConverterParams params);

	void doImport(Taxonomy taxonomy, SubTemporaryFile dataSourceFolder, Institution institution, ConverterParams params);

	/**
	 * @param parentFullPath
	 * @param term
	 */
	void validateTerm(Taxonomy taxonomy, String parentFullPath, String term, boolean requireParent)
		throws InvalidDataException;

	/**
	 * Get term result by uuid
	 * 
	 * @param taxonomy
	 * @param termUuid
	 * @return
	 */
	TermResult getTermResultByUuid(Taxonomy taxonomy, String termUuid);
}
