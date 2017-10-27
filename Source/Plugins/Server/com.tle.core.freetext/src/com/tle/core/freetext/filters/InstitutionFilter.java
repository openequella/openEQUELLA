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

package com.tle.core.freetext.filters;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;

public class InstitutionFilter extends Filter
{
	private static final long serialVersionUID = 1L;

	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException
	{
		int max = reader.maxDoc();
		OpenBitSet good = new OpenBitSet(max);
		Institution institution = CurrentInstitution.get();
		Term term = new Term(FreeTextQuery.FIELD_INSTITUTION, Long.toString(institution.getUniqueId()));
		TermDocs docs = reader.termDocs(term);
		while( docs.next() )
		{
			good.set(docs.doc());
		}
		docs.close();
		return good;
	}
}
