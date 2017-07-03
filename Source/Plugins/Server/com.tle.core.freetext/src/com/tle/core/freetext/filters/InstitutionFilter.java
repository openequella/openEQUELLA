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
