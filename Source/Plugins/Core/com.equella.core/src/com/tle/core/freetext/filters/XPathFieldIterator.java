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
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

@SuppressWarnings("nls")
public class XPathFieldIterator implements Iterator<Term>, Iterable<Term>
{
	private final String field;

	private TermEnum enumerator;
	private Term current;

	private Pattern pattern;

	public XPathFieldIterator(IndexReader reader, String field, String start) throws IOException
	{

		int hasIndex = field.indexOf('[');
		if( hasIndex >= 0 )
		{
			pattern = Pattern.compile(field.replaceAll("\\[\\]", "(\\\\[\\\\d*\\\\])?") + "/\\$XPATH\\$");
			field = field.substring(0, hasIndex);
		}
		enumerator = reader.terms(new Term(field, start));
		current = enumerator.term();
		this.field = field;
		findNextMatch();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return current != null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Term next()
	{
		try
		{
			Term result = current;
			goNext();
			findNextMatch();
			return result;
		}
		catch( IOException ex )
		{
			throw new RuntimeException("Error with Lucene TermEnum", ex);
		}
	}

	private void findNextMatch() throws IOException
	{
		while( current != null && !matches(current.field()) )
		{
			goNext();
		}

		if( current == null )
		{
			enumerator.close();
			enumerator = null;
		}
	}

	private void goNext() throws IOException
	{
		if( enumerator.next() )
		{
			current = enumerator.term();
			if( !current.field().startsWith(field) )
			{
				current = null;
			}
		}
		else
		{
			current = null;
		}
	}

	private boolean matches(String matchField)
	{
		if( pattern != null )
		{
			return pattern.matcher(matchField).matches();
		}
		return field.equals(matchField);
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Term> iterator()
	{
		return this;
	}
}
