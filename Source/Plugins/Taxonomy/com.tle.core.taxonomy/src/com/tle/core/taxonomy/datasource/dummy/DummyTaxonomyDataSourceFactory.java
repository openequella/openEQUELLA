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

package com.tle.core.taxonomy.datasource.dummy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import com.tle.common.beans.exception.InvalidDataException;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.common.Pair;
import com.tle.common.Utils;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.TaxonomyConstants;
import com.tle.core.guice.Bind;
import com.tle.core.taxonomy.TermResult;
import com.tle.core.taxonomy.datasource.TaxonomyDataSource;
import com.tle.core.taxonomy.datasource.TaxonomyDataSourceFactory;

/**
 * @author aholland
 */
@Bind
@Singleton
public class DummyTaxonomyDataSourceFactory implements TaxonomyDataSourceFactory
{
	@Override
	public TaxonomyDataSource create(Taxonomy taxonomy) throws Exception
	{
		return new DummyTaxonomyDataSource();
	}

	@SuppressWarnings("nls")
	public static class DummyTaxonomyDataSource implements TaxonomyDataSource
	{
		private static Map<String, DummyTermResult> termPaths = new HashMap<String, DummyTermResult>();
		private static Map<String, DummyTermResult> termNodeNames = new HashMap<String, DummyTermResult>();
		private static Map<DummyTermResult, List<DummyTermResult>> childMap = new HashMap<DummyTermResult, List<DummyTermResult>>();
		private static Map<DummyTermResult, DummyTermResult> parentMap = new HashMap<DummyTermResult, DummyTermResult>();
		private static DummyTermResult root;

		/*
		 * The HIGHLY scientific universal taxonomy
		 */
		static
		{
			root = makeTerm("universe", null);
			DummyTermResult animal = makeTerm("animal", root);
			DummyTermResult vegetable = makeTerm("vegetable", root);
			DummyTermResult mineral = makeTerm("mineral", root);

			// Animals
			DummyTermResult mammal = makeTerm("mammal", animal);
			makeTerm("cat", mammal);
			makeTerm("dog", mammal);
			makeTerm("buffalo", mammal);
			makeTerm("otter", mammal);

			DummyTermResult fish = makeTerm("fish", animal);
			makeTerm("trout", fish);
			makeTerm("salmon", fish);
			makeTerm("gummy shark", fish);
			makeTerm("great white shark", fish);

			DummyTermResult bird = makeTerm("bird", animal);
			makeTerm("emu", bird);
			makeTerm("goose", bird);
			makeTerm("duck", bird);
			makeTerm("swan", bird);
			makeTerm("starling", bird);
			makeTerm("robin", bird);
			makeTerm("ostrich", bird);
			makeTerm("dodo", bird);

			DummyTermResult reptile = makeTerm("reptile", animal);
			makeTerm("gecko", reptile);
			makeTerm("crocodile", reptile);
			makeTerm("komodo", reptile);
			makeTerm("blue tongue", reptile);

			DummyTermResult snake = makeTerm("snake", reptile);
			makeTerm("adder", snake);
			makeTerm("asp", snake);
			makeTerm("cobra", snake);
			makeTerm("python", snake);
			makeTerm("boa", snake);

			// Vegetables
			DummyTermResult tuber = makeTerm("tuber", vegetable);
			makeTerm("potato", tuber);

			// Minerals
			DummyTermResult metal = makeTerm("metal", mineral);
			makeTerm("gold", metal);
		}

		private static DummyTermResult makeTerm(String termName, DummyTermResult parent)
		{
			// all are assumed leaf until proven otherwise
			DummyTermResult term = new DummyTermResult(termName);
			termNodeNames.put(termName, term);
			childMap.put(term, new ArrayList<DummyTermResult>());
			if( parent != null )
			{
				childMap.get(parent).add(term);
				parentMap.put(term, parent);
				parent.setLeaf(false);
			}

			final String fullTermPath = calcFullTerm(term);
			term.setFullTerm(fullTermPath);
			termPaths.put(fullTermPath, term);

			return term;
		}

		private static String calcFullTerm(DummyTermResult term)
		{
			List<String> path = new ArrayList<String>();
			path.add(term.getTerm());

			DummyTermResult parent = term;
			while( (parent = parentMap.get(parent)) != null )
			{
				path.add(parent.getTerm());
			}
			Collections.reverse(path);
			return Utils.join(path.toArray(), TaxonomyConstants.TERM_SEPARATOR);
		}

		@Override
		public List<TermResult> getChildTerms(String parentTermId)
		{
			DummyTermResult term = (parentTermId == null ? root : termPaths.get(parentTermId));
			if( term == null )
			{
				return new ArrayList<TermResult>();
			}

			return Lists.transform(childMap.get(term), new Function<DummyTermResult, TermResult>()
			{
				@Override
				public TermResult apply(DummyTermResult t)
				{
					return t;
				}
			});
		}

		@Override
		public String getDataForTerm(String fullTermPath, String key)
		{
			DummyTermResult term = termPaths.get(fullTermPath);
			if( term != null )
			{
				return term.data.get(key);
			}
			return "";
		}

		@Override
		public TermResult getTerm(String fullTermPath)
		{
			return termPaths.get(fullTermPath);
		}

		/**
		 * Not terribly efficient, but hey, it's a dummy data source :) It also
		 * completely ignores the searchFullTerm parameter
		 */
		@Override
		public Pair<Long, List<TermResult>> searchTerms(String query, SelectionRestriction restriction, int limit,
			boolean searchFullTerm)
		{
			// turn it into a Regexp
			String queryTermName = getTermComponents(query).getSecond();
			Pattern p = Pattern.compile(queryTermName.replaceAll("\\*", ".*"), Pattern.CASE_INSENSITIVE);

			long resultCount = 0;
			List<TermResult> results = new ArrayList<TermResult>();
			for( String termName : termNodeNames.keySet() )
			{
				if( p.matcher(termName).matches() )
				{
					TermResult term = termNodeNames.get(termName);
					if( (restriction == SelectionRestriction.LEAF_ONLY && term.isLeaf())
						|| restriction == SelectionRestriction.UNRESTRICTED
						|| (restriction == SelectionRestriction.TOP_LEVEL_ONLY /*
																				 * &&
																				 * term
																				 * top
																				 * level
																				 * ?
																				 * ?
																				 */) )
					{
						resultCount++;
						if( resultCount <= limit )
						{
							results.add(termNodeNames.get(termName));
						}
					}
				}
			}

			return new Pair<Long, List<TermResult>>(resultCount, results);
		}

		/**
		 * Hax
		 * 
		 * @return The parent path and the term value
		 */
		private Pair<String, String> getTermComponents(String fullTermPath)
		{
			String parentFullPath = "";
			String termValue;

			int ind = fullTermPath.lastIndexOf(TaxonomyConstants.TERM_SEPARATOR);
			if( ind >= 0 )
			{
				parentFullPath = fullTermPath.substring(0, ind);
				termValue = fullTermPath.substring(ind + 1);
			}
			else
			{
				termValue = fullTermPath;
			}

			return new Pair<String, String>(parentFullPath, termValue);
		}

		@Override
		public TermResult addTerm(String parentFullPath, String termValue, boolean createHierarchy)
		{
			// see if it already exists
			final boolean rootTerm = Strings.isNullOrEmpty(parentFullPath);
			final String fullTermPath = (rootTerm ? termValue : parentFullPath + TaxonomyConstants.TERM_SEPARATOR
				+ termValue);
			TermResult existing = getTerm(fullTermPath);
			if( existing == null )
			{
				DummyTermResult parent = termPaths.get(parentFullPath);
				if( parent != null || rootTerm )
				{
					return makeTerm(termValue, parent);
				}
				else if( createHierarchy )
				{
					final String fullPathNoRoot = (parentFullPath.startsWith(TaxonomyConstants.TERM_SEPARATOR)
						? fullTermPath.substring(TaxonomyConstants.TERM_SEPARATOR.length()) : fullTermPath);
					final String[] parts = fullPathNoRoot.split(TaxonomyConstants.TERM_SEPARATOR_REGEX);

					StringBuilder path = new StringBuilder();
					DummyTermResult pterm = null;
					for( int i = 0; i < parts.length; i++ )
					{
						if( i > 0 )
						{
							path.append(TaxonomyConstants.TERM_SEPARATOR);
						}
						path.append(parts[i]);
						pterm = termPaths.get(path.toString());
						if( pterm == null )
						{
							pterm = makeTerm(termValue, parent);
						}
						parent = pterm;
					}
					return pterm;
				}
				else
				{
					throw new NotFoundException("Cannot find parent term " + parentFullPath);
				}
			}
			return existing;
		}

		@Override
		public void validateTerm(String parentFullTermPath, String termValue, boolean requireParent)
			throws InvalidDataException
		{
			// No holds barred
		}

		@Override
		public boolean supportsTermAddition()
		{
			return true;
		}

		@Override
		public boolean supportsTermBrowsing()
		{
			return true;
		}

		@Override
		public boolean supportsTermSearching()
		{
			return true;
		}

		public static class DummyTermResult extends TermResult
		{
			private Map<String, String> data = new HashMap<String, String>();

			public DummyTermResult(String term)
			{
				super(term, null, true);
			}
		}

		@Override
		public boolean isReadonly()
		{
			return false;
		}

		@Override
		public TermResult getTermByUuid(String termUuid)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDataByTermUuid(String termUuid, String dataKey)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, String> getAllDataByTermUuid(String termUuid)
		{
			return Maps.newHashMap();
		}
	}
}