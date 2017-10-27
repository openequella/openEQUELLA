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
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

import com.tle.common.searching.Field;

public class MustFilter extends Filter
{
	private static final long serialVersionUID = 1L;
	protected List<List<Field>> terms;

	public MustFilter(List<List<Field>> terms)
	{
		this.terms = terms;
	}

	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException
	{
		int max = reader.maxDoc();
		OpenBitSet prev = null;
		for( List<Field> values : terms )
		{
			if( !values.isEmpty() )
			{
				OpenBitSet good = new OpenBitSet(max);
				for( Field nv : values )
				{
					Term term = new Term(nv.getField(), nv.getValue());
					TermDocs docs = reader.termDocs(term);
					while( docs.next() )
					{
						good.set(docs.doc());
					}
					docs.close();
				}
				if( prev != null )
				{
					prev.and(good);
				}
				else
				{
					prev = good;
				}
			}
		}
		if( prev == null )
		{
			prev = new OpenBitSet(max);
			prev.set(0, max);
		}
		return prev;
	}

}
