package com.tle.core.freetext.filters;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;

import com.tle.common.searching.Field;

public class MustNotFilter extends MustFilter
{

	private static final long serialVersionUID = 1L;

	public MustNotFilter(List<List<Field>> terms)
	{
		super(terms);
	}

	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException
	{
		int max = reader.maxDoc();
		OpenBitSet good = new OpenBitSet(max);
		good.set(0, max);
		for( List<Field> values : terms )
		{
			for( Field nv : values )
			{
				Term term = new Term(nv.getField(), nv.getValue());
				TermDocs docs = reader.termDocs(term);
				while( docs.next() )
				{
					good.clear(docs.doc());
				}
				docs.close();
			}
		}
		return good;
	}

}
