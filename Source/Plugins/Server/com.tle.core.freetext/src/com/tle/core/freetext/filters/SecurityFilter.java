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
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;

public class SecurityFilter extends Filter
{
	private static final long serialVersionUID = 1L;

	private OpenBitSet results;
	private boolean onlyCollectResults;

	private String[] expressions;
	private Map<String, Boolean> ownerExprMap;
	private TermValueComparator comparator = new TermValueComparator();
	private int ownerSizes;
	private boolean systemUser;

	public SecurityFilter(String aclType)
	{
		ownerExprMap = new HashMap<String, Boolean>();

		UserState userState = CurrentUser.getUserState();
		systemUser = userState.isSystem();
		Collection<Long> aclExpressions = userState.getCommonAclExpressions();
		Collection<Long> ownerAclExpressions = userState.getOwnerAclExpressions();
		Collection<Long> notOwnerAclExpressions = userState.getNotOwnerAclExpressions();

		ownerSizes = (ownerAclExpressions == null ? 0 : ownerAclExpressions.size())
			+ (notOwnerAclExpressions == null ? 0 : notOwnerAclExpressions.size());
		expressions = new String[(aclExpressions == null ? 0 : aclExpressions.size()) + ownerSizes];

		int i = 0;
		if( aclExpressions != null )
		{
			for( Long acl : aclExpressions )
			{
				expressions[i++] = aclType + acl;
			}
		}

		if( ownerAclExpressions != null )
		{
			for( Long acl : ownerAclExpressions )
			{
				String aclStr = aclType + acl;
				expressions[i++] = aclStr;
				ownerExprMap.put(aclStr, true);
			}
		}
		if( notOwnerAclExpressions != null )
		{
			for( Long acl : notOwnerAclExpressions )
			{
				String aclStr = aclType + acl;
				expressions[i++] = aclStr;
				ownerExprMap.put(aclStr, false);
			}
		}
	}

	public OpenBitSet getResults()
	{
		return results;
	}

	public void setOnlyCollectResults(boolean onlyCollectResults)
	{
		this.onlyCollectResults = onlyCollectResults;
	}

	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException
	{
		final int max = reader.maxDoc();
		results = new OpenBitSet(max);

		if( !systemUser )
		{
			OpenBitSet owned = new OpenBitSet(max);
			if( ownerSizes > 0 )
			{
				TermDocs odocs = reader.termDocs(new Term(FreeTextQuery.FIELD_OWNER, CurrentUser.getUserID()));
				while( odocs.next() )
				{
					owned.set(odocs.doc());
				}
			}

			Set<Term> allTerms = new TreeSet<Term>(comparator);
			for( String element : expressions )
			{
				FieldIterator iterator = new FieldIterator(reader, element, ""); //$NON-NLS-1$
				while( iterator.hasNext() )
				{
					allTerms.add(iterator.next());
				}
			}

			for( Term term : allTerms )
			{
				String type = term.text();
				boolean grant = type.charAt(type.length() - 1) == 'G';
				TermDocs docs = reader.termDocs(term);
				Boolean exprType = ownerExprMap.get(term.field());
				if( exprType == null )
				{
					if( grant )
					{
						while( docs.next() )
						{
							results.set(docs.doc());
						}
					}
					else
					{
						while( docs.next() )
						{
							results.clear(docs.doc());
						}
					}
				}
				else
				{
					boolean must = exprType.booleanValue();
					while( docs.next() )
					{
						int doc = docs.doc();
						if( owned.get(doc) == must )
						{
							if( grant )
							{
								results.set(doc);
							}
							else
							{
								results.clear(doc);
							}
						}
					}
				}
				docs.close();
			}
		}
		else
		{
			TermDocs docs = reader.termDocs(null);
			while( docs.next() )
			{
				results.set(docs.doc());
			}
			docs.close();
		}

		// If we are only collecting results, we return a full bitset to match
		// every document.
		if( onlyCollectResults )
		{
			OpenBitSet fullBitSet = new OpenBitSet(max);
			fullBitSet.set(0, max);
			return fullBitSet;
		}
		else
		{
			return results;
		}
	}

	public static class TermValueComparator implements Comparator<Term>, Serializable
	{
		@Override
		public int compare(Term o1, Term o2)
		{
			int comp = o2.text().compareTo(o1.text());
			if( comp == 0 )
			{
				return o1.field().compareTo(o2.field());
			}
			return comp;
		}
	}
}
