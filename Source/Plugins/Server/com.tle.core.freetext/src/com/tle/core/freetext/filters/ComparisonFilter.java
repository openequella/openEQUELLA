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
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

/**
 * Filters string by comparison. This is inclusive.
 * 
 * @author Nicholas Read
 */
public class ComparisonFilter extends Filter
{
	private static final long serialVersionUID = 1L;
	private final String field;
	private final String start;
	private final String end;

	public ComparisonFilter(String field, String start, String end)
	{
		this.field = field;
		this.start = start;
		this.end = end;
	}

	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException
	{
		OpenBitSet bits = new OpenBitSet(reader.maxDoc());

		Term startTerm = new Term(field, start);
		Term endTerm = new Term(field, end);

		TermEnum enumerator = reader.terms(startTerm);
		if( enumerator.term() == null )
		{
			return bits;
		}

		TermDocs termDocs = reader.termDocs();
		try
		{
			Term current = enumerator.term();
			while( current.compareTo(endTerm) <= 0 )
			{
				termDocs.seek(enumerator.term());
				while( termDocs.next() )
				{
					bits.set(termDocs.doc());
				}

				if( !enumerator.next() )
				{
					break;
				}

				current = enumerator.term();
			}
		}
		finally
		{
			enumerator.close();
			termDocs.close();
		}

		return bits;
	}
}
