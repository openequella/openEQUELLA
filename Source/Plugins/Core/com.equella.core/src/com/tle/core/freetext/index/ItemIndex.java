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

package com.tle.core.freetext.index;

import it.uniroma3.mat.extendedset.intset.ConciseSet;
import it.uniroma3.mat.extendedset.intset.FastSet;
import it.uniroma3.mat.extendedset.wrappers.LongSet;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ChainedFilter;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.FieldCacheRangeFilter;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.NRTManager;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.Version;

import com.dytech.edge.exceptions.InvalidDateRangeException;
import com.dytech.edge.exceptions.InvalidSearchQueryException;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.searching.DateFilter.Format;
import com.tle.common.searching.Field;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;
import com.tle.common.searching.SimpleSearchResults;
import com.tle.common.searching.SortField.Type;
import com.tle.common.settings.standard.SearchSettings;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.common.util.TleDate;
import com.tle.core.freetext.filters.DateFilter;
import com.tle.core.freetext.filters.InstitutionFilter;
import com.tle.core.freetext.filters.MustFilter;
import com.tle.core.freetext.filters.MustNotFilter;
import com.tle.core.freetext.filters.SecurityFilter;
import com.tle.core.freetext.filters.XPathFieldIterator;
import com.tle.core.freetext.queries.FreeTextAutocompleteQuery;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.queries.FreeTextDateQuery;
import com.tle.core.freetext.queries.FreeTextFieldExistsQuery;
import com.tle.core.freetext.queries.FreeTextFieldQuery;
import com.tle.core.remoting.MatrixResults;
import com.tle.core.services.item.FreetextResult;
import com.tle.freetext.FreetextIndex;
import com.tle.freetext.IndexedItem;
import com.tle.freetext.LuceneConstants;
import com.tle.freetext.TLEQueryParser;

@SuppressWarnings("nls")
@NonNullByDefault
public abstract class ItemIndex<T extends FreetextResult> extends AbstractIndexEngine
{
	private static final Pattern AND = Pattern.compile("(\\W)and(\\W)"); //$NON-NLS-1$
	private static final Pattern OR = Pattern.compile("(\\W)or(\\W)"); //$NON-NLS-1$
	private static final Pattern NOT = Pattern.compile("(\\W)not(\\W)"); //$NON-NLS-1$

	private static final Map<String, String> PRIV_MAP = ImmutableMap.of("MODERATE_ITEM", "ACLM-", "VIEW_ITEM", "ACLV-",
		"DISCOVER_ITEM", "ACLD-", "DELETE_ITEM", "ACLR-", "DOWNLOAD_ITEM", "ACLL-");

	@Inject
	private FreetextIndex freetextIndex;

	private float titleBoost;
	private float descriptionBoost;
	private float attachmentBoost;

	/**
	 * AND or OR
	 */
	private String defaultOperator;
	private FieldSelector keyFieldSelector;

	@PostConstruct
	@Override
	public void afterPropertiesSet() throws IOException
	{
		setStopWordsFile(freetextIndex.getStopWordsFile());
		setDefaultOperator(freetextIndex.getDefaultOperator());
		keyFieldSelector = new SetBasedFieldSelector(getKeyFields(), new HashSet<String>());

		super.afterPropertiesSet();
	}

	private float getRealBoostValue(int savedBoostValue)
	{
		switch( savedBoostValue )
		{
			case 0:
				return 0;
			case 1:
				return 0.25f;
			case 2:
				return 0.5f;
			case 3:
				return 1;
			case 4:
				return 1.5f;
			case 5:
				return 2;
			case 6:
				return 4;
			case 7:
				return 8;
		}
		return -1;
	}

	protected Set<String> getKeyFields()
	{
		return new HashSet<String>(Arrays.asList(FreeTextQuery.FIELD_UNIQUE, FreeTextQuery.FIELD_ID));
	}

	public void setTitleBoost(float titleBoost)
	{
		this.titleBoost = titleBoost;
	}

	public void setDescriptionBoost(float descriptionBoost)
	{
		this.descriptionBoost = descriptionBoost;
	}

	public void setAttachmentBoost(float attachmentBoost)
	{
		this.attachmentBoost = attachmentBoost;
	}

	protected long removeDocuments(Collection<IndexedItem> documents, NRTManager nrtManager)
	{
		long generation = -1;
		for( IndexedItem item : documents )
		{
			ItemIdKey itemId = item.getItemIdKey();
			String unique = ItemId.fromKey(itemId).toString();
			try
			{
				BooleanQuery delQuery = new BooleanQuery();
				delQuery.add(new TermQuery(new Term(FreeTextQuery.FIELD_ID, Long.toString(itemId.getKey()))),
					Occur.MUST);
				delQuery.add(
					new TermQuery(new Term(FreeTextQuery.FIELD_INSTITUTION, Long.toString(item.getInstitution()
						.getUniqueId()))), Occur.MUST);
				long g = nrtManager.deleteDocuments(delQuery);

				if( item.isNewSearcherRequired() )
				{
					generation = g;
				}
			}
			catch( IOException e )
			{
				LOGGER.error("An error occurred while attempting to delete item " + unique, e); //$NON-NLS-1$
				throw new RuntimeException(e);
			}
		}
		return generation;
	}

	public long addDocuments(Collection<IndexedItem> documents, NRTManager nrtManager)
	{
		long generation = -1;
		for( IndexedItem item : documents )
		{
			if( item.isAdd() )
			{
				synchronized( item )
				{
					Document doc = item.getItemdoc();
					if( doc.getFields().isEmpty() )
					{
						LOGGER.error("Trying to add an empty document for item:" + item.getId());
						continue;
					}
					try
					{
						long g = nrtManager.addDocument(doc);
						if( item.isNewSearcherRequired() )
						{
							generation = g;
						}
					}
					catch( IOException e )
					{
						throw new RuntimeException(e);
					}
				}
			}
		}
		return generation;
	}

	/**
	 * Performs a search and returns the results for the given page.
	 * 
	 * @param requestedStart 0 or greater.
	 * @param requestedCount 0 or greater. Less than one returns all results.
	 */
	public SearchResults<T> search(final Search searchreq, final int requestedStart, final int requestedCount,
		final boolean searchAttachment)
	{
		return search(new Searcher<SearchResults<T>>()
		{
			@Override
			public SearchResults<T> search(IndexSearcher searcher) throws IOException
			{
				long t1 = System.currentTimeMillis();

				Collection<Filter> filters = getFilters(searchreq);
				SecurityFilter deleteablesFilter = null;
				String collectPriv = searchreq.getPrivilegeToCollect();
				if( collectPriv != null )
				{
					deleteablesFilter = new SecurityFilter(getPrefixForPrivilege(collectPriv));
					deleteablesFilter.setOnlyCollectResults(true);
					filters.add(deleteablesFilter);
				}
				Filter filter = new ChainedFilter(filters.toArray(new Filter[filters.size()]), ChainedFilter.AND);

				Sort sorter = getSorter(searchreq);

				// TODO: We should really be doing a
				// Preconditions.checkArgument(requestedStart >= 0) and the same
				// for requestedCount too rather than trying to just work.
				int actualStart = Math.max(0, requestedStart);
				int actualCount = -1;
				int numDocs = searcher.getIndexReader().numDocs();

				if( requestedCount < 0 || requestedCount == Integer.MAX_VALUE || requestedCount > numDocs )
				{
					actualCount = numDocs;
				}
				else
				{
					actualCount = requestedCount + actualStart;
					if( actualCount > numDocs )
					{
						actualCount = numDocs;
					}
				}

				SearchResults<T> results;
				Query query = getQuery(searchreq, searcher.getIndexReader(), searchAttachment);
				boolean searchAll = Check.isEmpty(searchreq.getQuery()) || searchreq.getQuery().equals("*");

				if( actualCount == 0 )
				{
					TotalHitCountCollector hitCount = new TotalHitCountCollector();
					searcher.search(query, filter, hitCount);
					results = new SimpleSearchResults<T>(new ArrayList<T>(), 0, 0, hitCount.getTotalHits());
				}
				else
				{
					final TopDocs hits = searcher.search(query, filter, actualCount, sorter);
					final SearchResults<T> itemResults = getResultsFromTopDocs(searcher, hits, actualStart,
						deleteablesFilter, searchreq);

					SearchResults<T> attachmentResults = null;
					int attachmentBoostValue = freetextIndex.getSearchSettings().getAttachmentBoost();
					if( searchAll )
					{
						// don't worry about attachment relevance
						results = itemResults;
					}
					else if( attachmentBoostValue != 0 && searchAttachment )
					{
						final String[] fields = new String[]{FreeTextQuery.FIELD_ATTACHMENT_VECTORED};

						Query queryAttachmentOnly = getQuery(searchreq, searcher.getIndexReader(), fields,
							searchAttachment);
						queryAttachmentOnly = addUniqueIdClauseToQuery(queryAttachmentOnly, itemResults,
							searcher.getIndexReader());

						final TopDocs attachmentHits = searcher
							.search(queryAttachmentOnly, filter, actualCount, sorter);
						attachmentResults = getResultsFromTopDocs(searcher, attachmentHits, 0, deleteablesFilter,
							searchreq);
					}

					if( attachmentResults != null )
					{
						results = markItemKeywordFoundInAttachment(itemResults, attachmentResults);
					}
					else
					{
						results = itemResults;
					}
				}
				long t2 = System.currentTimeMillis();

				if( !(searchreq.getFreeTextQuery() instanceof FreeTextAutocompleteQuery) )
				{
					LOGGER.info("Query[" + query + "] Hits[" + results.getAvailable() + "] Returning["
						+ results.getCount() + "] Time Elapsed[" + (t2 - t1) + "ms] Total Indexed[" + numDocs + "]");
				}

				return results;
			}
		});
	}

	private Query addUniqueIdClauseToQuery(Query query, SearchResults<T> itemResults, IndexReader reader)
	{
		List<T> results = itemResults.getResults();
		BooleanQuery orQuery = new BooleanQuery();

		for( T t : results )
		{
			ItemIdKey itemIdKey = t.getItemIdKey();
			String[] split = itemIdKey.toString().split("/");
			String uniqueId = split[1] + "/" + split[2];

			FreeTextBooleanQuery bquery = new FreeTextBooleanQuery(false, true);
			bquery.add(new FreeTextFieldQuery(FreeTextQuery.FIELD_UNIQUE, uniqueId));
			BooleanClause convertBoolean = convertToBooleanClause(bquery, reader);
			convertBoolean.setOccur(Occur.SHOULD);
			orQuery.add(convertBoolean);
		}

		BooleanQuery newQuery = new BooleanQuery();
		newQuery.add(query, Occur.MUST);
		newQuery.add(orQuery, Occur.MUST);

		return newQuery;
	}

	private SearchResults<T> markItemKeywordFoundInAttachment(SearchResults<T> results,
		SearchResults<T> attachmentResults)
	{
		List<T> attachments = attachmentResults.getResults();

		for( int i = 0; i < results.getResults().size(); i++ )
		{
			T t = results.getResults().get(i);
			t.setKeywordFoundInAttachment(false);

			for( T attachment : attachments )
			{
				if( t.getItemIdKey().equals(attachment.getItemIdKey()) )
				{
					t.setKeywordFoundInAttachment(true);
					break;
				}
			}
		}
		return results;
	}

	public LongSet searchBitSet(final Search searchreq, final boolean searchAttachments)
	{
		return search(new Searcher<LongSet>()
		{
			@Override
			public LongSet search(IndexSearcher searcher) throws IOException
			{
				long t1 = System.currentTimeMillis();

				Collection<Filter> filters = getFilters(searchreq);
				SecurityFilter deleteablesFilter = null;
				String collectPriv = searchreq.getPrivilegeToCollect();
				if( collectPriv != null )
				{
					deleteablesFilter = new SecurityFilter(getPrefixForPrivilege(collectPriv));
					deleteablesFilter.setOnlyCollectResults(true);
					filters.add(deleteablesFilter);
				}
				Filter filter = new ChainedFilter(filters.toArray(new Filter[filters.size()]), ChainedFilter.AND);

				IndexReader indexReader = searcher.getIndexReader();
				Query query = getQuery(searchreq, indexReader, searchAttachments);
				int numDocs = indexReader.numDocs();

				CompressedSetCollector compCollector = new CompressedSetCollector();
				searcher.search(query, filter, compCollector);
				LongSet docIdSet = compCollector.getSet();

				LongSet longSet = new LongSet(new FastSet());
				FieldSelector fieldSelector = new ItemIdFieldSelector();
				Iterator<Long> iterator = docIdSet.iterator();
				while( iterator.hasNext() )
				{
					long doc = iterator.next();
					Document document = indexReader.document((int) doc, fieldSelector);
					long key = Long.parseLong(document.get(FreeTextQuery.FIELD_ID));
					longSet.add(key);
				}
				LongSet results = new LongSet(new ConciseSet());
				results.addAll(longSet);

				long t2 = System.currentTimeMillis();
				long size = results.size();
				LOGGER.info("Query[" + query + "] Hits[" + size + "] Returning[" + size + "] Time Elapsed[" + (t2 - t1)
					+ "ms] Total Indexed[" + numDocs + "]");

				return results;
			}
		});
	}

	public static String convertStdPriv(String priv)
	{
		String pfx = PRIV_MAP.get(priv);
		if( pfx == null )
		{
			throw new Error("Unknown prefix for:" + priv); //$NON-NLS-1$
		}
		return pfx;
	}

	protected String getPrefixForPrivilege(String priv)
	{
		return convertStdPriv(priv);
	}

	protected LongSet getBitSetFromTopDocs(IndexSearcher searcher, TopDocs hits, int firstHit) throws IOException
	{
		LongSet longSet = new LongSet(new ConciseSet());
		ScoreDoc[] results = hits.scoreDocs;
		if( firstHit < results.length )
		{
			for( int i = firstHit; i < results.length; i++ )
			{
				int docId = results[i].doc;
				Document doc = searcher.doc(docId, keyFieldSelector);
				ItemIdKey key = getKeyForDocument(doc);
				longSet.add(key.getKey());
			}
		}
		return longSet;
	}

	protected SearchResults<T> getResultsFromTopDocs(IndexSearcher searcher, TopDocs hits, int firstHit,
		@Nullable SecurityFilter deleteables, Search originalSearch) throws IOException
	{
		List<T> retrievedResults = new ArrayList<T>();

		OpenBitSet deleteableDocIds = null;
		if( deleteables != null )
		{
			deleteableDocIds = deleteables.getResults();
		}

		boolean sortByRelevance = false;
		com.tle.common.searching.SortField[] sortfields = originalSearch.getSortFields();
		if( sortfields != null )
		{
			for( com.tle.common.searching.SortField sortfield : sortfields )
			{
				if( sortfield.getType() == Type.SCORE )
				{
					sortByRelevance = true;
					searcher.setDefaultFieldSortScoring(true, false);
				}
			}
		}

		ScoreDoc[] results = hits.scoreDocs;
		if( firstHit < results.length )
		{
			for( int i = firstHit; i < results.length; i++ )
			{
				int docId = results[i].doc;
				float relevance = results[i].score;
				Document doc = searcher.doc(docId, keyFieldSelector);
				ItemIdKey key = getKeyForDocument(doc);
				T result = createResult(key, doc, relevance, sortByRelevance);
				if( deleteableDocIds != null && deleteableDocIds.get(docId) )
				{
					result.setMatchesPrivilege(true);
				}
				retrievedResults.add(result);

			}
		}

		return new SimpleSearchResults<T>(retrievedResults, retrievedResults.size(), firstHit, hits.totalHits);
	}

	protected abstract T createResult(ItemIdKey key, Document doc, float relevance, boolean sortByRelevance);

	protected ItemIdKey getKeyForDocument(Document doc)
	{
		String id = doc.get(FreeTextQuery.FIELD_UNIQUE);
		long key = Long.parseLong(doc.get(FreeTextQuery.FIELD_ID));
		return new ItemIdKey(key, new ItemId(id));
	}

	/**
	 * Counts the number of documents that a query matches without actually
	 * retrieving the documents. Should be pretty damn fast.
	 */
	public int count(final Search searchreq, final boolean isSearchAttachment)
	{
		return search(new Searcher<Integer>()
		{
			@Override
			public Integer search(IndexSearcher searcher) throws IOException
			{
				Filter filters = getFilter(searchreq);
				Query query = getQuery(searchreq, null, isSearchAttachment);

				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Counting for " + query);
				}

				CountingCollector collector = new CountingCollector();
				searcher.search(query, filters, collector);

				int count = collector.getCount();
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Counted " + count + " items");
				}
				return count;
			}
		});
	}

	/**
	 * A simplified implementation of matrixSearch() that only works on a single
	 * field, and currently only returns the count per term. It could easily be
	 * extended to return a list of ItemIds per term, it simply wasn't necessary
	 * when I was writing it!
	 * <p>
	 * This simplified implementation was written to overcome the memory
	 * pressures that matrixSearch() creates when you have over half a million
	 * terms for a field. MatrixSearch() creates *many* BitSets that it holds on
	 * to to reuse as it recurse through a list of fields. Since we only care
	 * about a single field in this implementation, we can avoid generating and
	 * holding onto BitSets.
	 */
	public Multimap<String, Pair<String, Integer>> facetCount(@Nullable final Search searchreq,
		final Collection<String> fields)
	{
		return search(new Searcher<Multimap<String, Pair<String, Integer>>>()
		{
			@Override
			public Multimap<String, Pair<String, Integer>> search(IndexSearcher searcher) throws IOException
			{
				final IndexReader reader = searcher.getIndexReader();
				final OpenBitSet filteredBits = searchRequestToBitSet(searchreq, searcher, reader);

				final Multimap<String, Pair<String, Integer>> rv = ArrayListMultimap.create();
				for( String field : fields )
				{
					for( Term term : new XPathFieldIterator(reader, field, "") )
					{
						int count = 0;

						TermDocs docs = reader.termDocs(term);
						while( docs.next() )
						{
							if( filteredBits.get(docs.doc()) )
							{
								count++;
							}
						}
						docs.close();

						if( count > 0 )
						{
							rv.put(field, new Pair<String, Integer>(term.text(), count));
						}
					}
				}
				return rv;
			}
		});
	}

	public MatrixResults matrixSearch(@Nullable final Search searchreq, final List<String> fields,
		final boolean countOnly)
	{
		return search(new Searcher<MatrixResults>()
		{
			@Override
			public MatrixResults search(IndexSearcher searcher) throws IOException
			{
				IndexReader reader = searcher.getIndexReader();

				OpenBitSet filteredBits = searchRequestToBitSet(searchreq, searcher, reader);
				int maxDoc = reader.maxDoc();

				Map<String, Map<String, List<TermBitSet>>> xpathMap = new HashMap<String, Map<String, List<TermBitSet>>>();

				List<OpenBitSet> perFieldBitSets = new ArrayList<OpenBitSet>();
				OpenBitSet allDocs = new OpenBitSet();
				for( String field : fields )
				{
					boolean hasXpaths = field.indexOf('[') != -1;
					OpenBitSet perFieldBitSet = new OpenBitSet(maxDoc);
					for( Term term : new XPathFieldIterator(reader, field, "") )
					{
						OpenBitSet set = new OpenBitSet(maxDoc);
						TermDocs docs = reader.termDocs(term);
						while( docs.next() )
						{
							set.set(docs.doc());
						}
						docs.close();
						perFieldBitSet.or(set);
						allDocs.or(set);
						String xpathKey = "";
						if( hasXpaths )
						{
							String fieldName = term.field();
							int ind = fieldName.lastIndexOf(']');
							if( ind != -1 )
							{
								xpathKey = fieldName.substring(0, ind + 1);
							}
						}
						addFieldBitSet(term, set, xpathKey, xpathMap, field);
					}
					// Simulate blank Term's for each field, that has at least
					// one
					// matching field
					addFieldBitSet(new Term(field, ""), perFieldBitSet, "", xpathMap, field);
					perFieldBitSets.add(perFieldBitSet);
				}
				for( OpenBitSet set : perFieldBitSets )
				{
					set.xor(allDocs);
				}

				MatrixResults results = new MatrixResults();
				results.setFields(fields);

				Map<String, List<TermBitSet>> blankPaths = xpathMap.get("");
				for( Map<String, List<TermBitSet>> map : xpathMap.values() )
				{
					List<List<TermBitSet>> fieldsToRecurse = new ArrayList<List<TermBitSet>>();
					for( String field : fields )
					{
						List<TermBitSet> list = map.get(field);
						if( list == null )
						{
							list = blankPaths.get(field);
						}
						fieldsToRecurse.add(list);
					}
					recurseTerms(fieldsToRecurse, 0, new String[fields.size()], filteredBits, results, reader,
						countOnly);
				}
				return results;
			}
		});
	}

	private OpenBitSet searchRequestToBitSet(@Nullable final Search searchreq, IndexSearcher searcher,
		IndexReader reader) throws IOException
	{
		if( searchreq != null )
		{
			Filter filters = getFilter(searchreq);
			Query query = getQuery(searchreq, null, false);

			BitSetCollector collector = new BitSetCollector();
			searcher.search(query, filters, collector);
			return collector.getBitSet();
		}
		else
		{
			return (OpenBitSet) new InstitutionFilter().getDocIdSet(reader);
		}
	}

	public String suggestTerm(final Search request, final String prefix, final boolean isSearchAttachment)
	{
		return search(new Searcher<String>()
		{
			@Override
			public String search(IndexSearcher searcher) throws IOException
			{
				// Get the reader
				IndexReader reader = searcher.getIndexReader();

				// Get all the docs from the filters
				Collection<Filter> filters = getFilters(request);
				Query query = getQuery(request, reader, isSearchAttachment);
				filters.add(new QueryWrapperFilter(query));
				ChainedFilter chain = new ChainedFilter(filters.toArray(new Filter[filters.size()]), ChainedFilter.AND);
				DocIdSetIterator iterator = chain.getDocIdSet(reader).iterator();

				// Get docs that contain terms that begin with the prefix
				List<Term> termList = Lists.newArrayList();
				termList.add(new Term(FreeTextQuery.FIELD_BODY_NOSTEM, prefix));
				termList.add(new Term(FreeTextQuery.FIELD_ATTACHMENT_VECTORED_NOSTEM, prefix));

				for( Term term : termList )
				{
					TermDocs termDocs = reader.termDocs(term);
					TermEnum terms = reader.terms(term);

					// Check if doc is in the filter and return the term
					Term t;
					for( t = terms.term(); t != null && t.text().startsWith(prefix); t = terms.term() )
					{
						termDocs.seek(t);
						while( termDocs.next() )
						{
							int docId = termDocs.doc();
							if( docId == iterator.advance(docId) )
							{
								return t.text();
							}
						}
						terms.next();
					}
				}
				return "";
			}
		});
	}

	private void addFieldBitSet(Term term, OpenBitSet set, String xpathKey,
		Map<String, Map<String, List<TermBitSet>>> xpathMap, String field)
	{
		Map<String, List<TermBitSet>> map = xpathMap.get(xpathKey);
		if( map == null )
		{
			map = new HashMap<String, List<TermBitSet>>();
			xpathMap.put(xpathKey, map);
		}
		List<TermBitSet> list = map.get(field);
		if( list == null )
		{
			list = new ArrayList<TermBitSet>();
			map.put(field, list);
		}
		list.add(new TermBitSet(term, set));
	}

	private final class ItemIdFieldSelector implements FieldSelector
	{
		@Override
		public FieldSelectorResult accept(String fieldName)
		{
			if( FreeTextQuery.FIELD_ID.equals(fieldName) )
			{
				return FieldSelectorResult.LOAD_AND_BREAK;
			}
			else
			{
				return FieldSelectorResult.NO_LOAD;
			}
		}
	}

	protected final class CompressedSetCollector extends Collector
	{

		private final LongSet set = new LongSet(new ConciseSet());
		private int docBase;

		@Override
		public void setScorer(Scorer scorer) throws IOException
		{
			// nothing
		}

		@Override
		public void setNextReader(IndexReader reader, int docBase) throws IOException
		{
			this.docBase = docBase;
		}

		@Override
		public void collect(int docId) throws IOException
		{
			set.add(docBase + docId);
		}

		@Override
		public boolean acceptsDocsOutOfOrder()
		{
			return true;
		}

		public LongSet getSet()
		{
			return set;
		}
	}

	public static class TermBitSet
	{
		Term term;
		OpenBitSet bitSet;

		public TermBitSet(Term term, OpenBitSet bitSet)
		{
			this.term = term;
			this.bitSet = bitSet;
		}
	}

	private void recurseTerms(List<List<TermBitSet>> bitSets, int index, String[] curValues, OpenBitSet curBits,
		MatrixResults results, IndexReader reader, boolean countOnly)
	{
		List<TermBitSet> termBitSetList = bitSets.get(index);
		boolean last = index == curValues.length - 1;
		for( TermBitSet termBitSet : termBitSetList )
		{
			OpenBitSet termBits = termBitSet.bitSet;
			Term term = termBitSet.term;
			// if we don't intersect there's no point in recursing further in
			if( curBits.intersects(termBits) )
			{
				// Collect current term's value into the value array
				curValues[index] = term.text();
				OpenBitSet docBits = (OpenBitSet) curBits.clone();
				docBits.and(termBits);
				if( last )
				{
					int count;
					List<ItemIdKey> ids = null;
					ArrayList<String> vals = new ArrayList<String>(Arrays.asList(curValues));
					if( !countOnly )
					{
						ids = getIdsForBitset(docBits, reader);
						count = ids.size();
					}
					else
					{
						count = (int) docBits.cardinality();
					}
					results.addEntry(new MatrixResults.MatrixEntry(vals, ids, count));
				}
				else
				{
					recurseTerms(bitSets, index + 1, curValues, docBits, results, reader, countOnly);
				}
			}
		}
	}

	private List<ItemIdKey> getIdsForBitset(OpenBitSet docBits, IndexReader reader)
	{
		int docid = 0;
		List<ItemIdKey> keys = new ArrayList<ItemIdKey>();
		while( true )
		{
			docid = docBits.nextSetBit(docid);
			if( docid == -1 )
			{
				break;
			}
			Document doc;
			try
			{
				doc = reader.document(docid);
			}
			catch( IOException e )
			{
				throw new RuntimeException(e);
			}
			ItemIdKey key = getKeyForDocument(doc);
			keys.add(key);
			docid++;
		}
		return keys;
	}

	/**
	 * Takes a search request and prepares a Lucene Query object.
	 */
	protected Query getQuery(Search request, IndexReader reader, boolean searchAttachment)
	{
		final String[] fields = FreeTextQuery.BASIC_NAME_BODY_ATTACHMENT_FIELDS
			.toArray(new String[FreeTextQuery.BASIC_NAME_BODY_ATTACHMENT_FIELDS.size()]);
		return getQuery(request, reader, fields, searchAttachment);
	}

	private Query getQuery(Search request, IndexReader reader, String[] allFields, boolean searchAttachment)
	{
		List<String> searchFields = new ArrayList<String>();
		for( String string : allFields )
		{
			searchFields.add(string);
		}

		SearchSettings searchSettings = freetextIndex.getSearchSettings();

		float titleBoostValue = getRealBoostValue(searchSettings.getTitleBoost());
		if( titleBoostValue == 0 )
		{
			searchFields.remove(FreeTextQuery.FIELD_NAME_VECTORED);
		}
		setTitleBoost(titleBoostValue == -1 ? 1 : titleBoostValue);

		float descriptionBoostValue = getRealBoostValue(searchSettings.getDescriptionBoost());

		if( descriptionBoostValue == 0 )
		{
			searchFields.remove(FreeTextQuery.FIELD_BODY);
		}
		setDescriptionBoost(descriptionBoostValue == -1 ? 1 : descriptionBoostValue);

		float attachmentBoostValue = getRealBoostValue(searchSettings.getAttachmentBoost());
		if( attachmentBoostValue == 0 || !searchAttachment )
		{
			searchFields.remove(FreeTextQuery.FIELD_ATTACHMENT_VECTORED);
		}
		setAttachmentBoost(attachmentBoostValue == -1 ? 1 : attachmentBoostValue);

		final String[] fields = searchFields.toArray(new String[searchFields.size()]);

		Query query;

		String queryString = request.getQuery();
		try
		{
			if( Check.isEmpty(queryString) || queryString.trim().equals("*") )
			{
				query = new TermQuery(new Term(FreeTextQuery.FIELD_ALL, "1"));
			}
			else
			{
				queryString = queryString.toLowerCase();
				queryString = AND.matcher(queryString).replaceAll("$1AND$2");
				queryString = OR.matcher(queryString).replaceAll("$1OR$2");
				queryString = NOT.matcher(queryString).replaceAll("$1NOT$2");

				Version luceneVersion = LuceneConstants.LATEST_VERSION;

				Map<String, Float> boosts = Maps.newHashMap();
				boosts.put(FreeTextQuery.FIELD_NAME_VECTORED, titleBoost);
				boosts.put(FreeTextQuery.FIELD_BODY, descriptionBoost);
				boosts.put(FreeTextQuery.FIELD_ATTACHMENT_VECTORED, attachmentBoost);

				TLEQueryParser tleParser = new TLEQueryParser(luceneVersion, fields, getAnalyser(), boosts);
				tleParser.setDefaultOperator(getDefaultOperator());
				Query tleQuery = tleParser.parse(queryString);

				BooleanQuery orQuery = new BooleanQuery(true);
				orQuery.add(tleQuery, Occur.SHOULD);

				List<String> queries = request.getExtraQueries();
				if( queries != null )
				{
					for( String queryStr : queries )
					{
						orQuery.add(tleParser.parse(queryStr), Occur.SHOULD);
					}
				}
				query = orQuery;
			}
			query = addExtraQuery(query, request, reader);
		}
		catch( ParseException ex )
		{
			throw new InvalidSearchQueryException(queryString, ex);
		}
		return query;
	}

	protected Filter getFilter(Search request)
	{
		Collection<Filter> filters = getFilters(request);
		return new ChainedFilter(filters.toArray(new Filter[filters.size()]), ChainedFilter.AND);
	}

	/**
	 * Takes a search request and prepares a Lucene Filter object, or null if no
	 * filtering is required.
	 */
	protected Collection<Filter> getFilters(Search request)
	{
		List<Filter> filters = Lists.newArrayList();

		Date[] dateRange = request.getDateRange();
		if( dateRange != null )
		{
			filters.add(createDateFilter(FreeTextQuery.FIELD_REALLASTMODIFIED, dateRange, Dates.ISO));
		}

		Collection<com.tle.common.searching.DateFilter> dateFilters = request.getDateFilters();
		if( dateFilters != null )
		{
			for( com.tle.common.searching.DateFilter dateFilter : dateFilters )
			{
				Date[] range = dateFilter.getRange();
				String indexFieldName = dateFilter.getIndexFieldName();
				if( dateFilter.getFormat() == Format.ISO )
				{
					filters.add(createDateFilter(indexFieldName, range, Dates.ISO));
				}
				else
				{
					Long start = range[0] != null ? range[0].getTime() : null;
					Long end = range[1] != null ? range[1].getTime() : null;
					filters.add(FieldCacheRangeFilter.newLongRange(indexFieldName, start, end, true, true));
				}
			}
		}

		String privPrefix = request.getPrivilegePrefix();
		String privilege = request.getPrivilege();
		if( privPrefix == null && privilege != null )
		{
			privPrefix = getPrefixForPrivilege(privilege);
		}
		if( privPrefix != null )
		{
			filters.add(new SecurityFilter(privPrefix));
		}

		List<List<Field>> must = request.getMust();
		List<List<Field>> mustNot = request.getMustNot();
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Must " + must + ": Must Not: " + mustNot + " Privilege:" + privilege);
		}
		if( must != null && !must.isEmpty() )
		{
			filters.add(new MustFilter(must));
		}
		if( mustNot != null && !mustNot.isEmpty() )
		{
			filters.add(new MustNotFilter(mustNot));
		}
		List<Field> matrixFields = request.getMatrixFields();
		if( matrixFields != null )
		{
			filters.add(new MatrixFilter(matrixFields));
		}
		filters.add(new InstitutionFilter());
		return filters;
	}

	protected DateFilter createDateFilter(String fieldName, Date[] range, Dates indexDateFormat)
	{
		if( range.length != 2 || (range[0] != null && range[1] != null && range[0].after(range[1])) )
		{
			throw new InvalidDateRangeException();
		}
		return new DateFilter(fieldName, range[0], range[1], indexDateFormat, null);
	}

	/**
	 * Takes a search request and prepares a Lucene Sort object, or null if no
	 * sorting is required.
	 */
	private Sort getSorter(Search request)
	{
		com.tle.common.searching.SortField[] sortfields = request.getSortFields();
		if( sortfields != null )
		{
			SortField[] convFields = new SortField[sortfields.length];
			int i = 0;
			for( com.tle.common.searching.SortField sortfield : sortfields )
			{
				FieldComparatorSource fieldComparatorSource = null;
				int type = SortField.STRING;
				switch( sortfield.getType() )
				{
					case INT:
						type = SortField.INT;
						break;
					case LONG:
						type = SortField.LONG;
						break;
					case SCORE:
						type = SortField.SCORE;
						break;
					case CUSTOM:
						type = SortField.CUSTOM;
						fieldComparatorSource = sortfield.getFieldComparatorSource();
					default:
						// Stays STRING
						break;
				}
				// @formatter:off
				SortField sortField = fieldComparatorSource != null
					? new SortField(sortfield.getField(), fieldComparatorSource, request.isSortReversed())
						: new SortField(sortfield.getField(), type, sortfield.isReverse() ^ request.isSortReversed());
				// @formatter:on
				convFields[i++] = sortField;
			}
			return new Sort(convFields);
		}
		return new Sort(new SortField(null, SortField.SCORE, false));
	}

	/**
	 * @dytech.jira see Jira Review TLE-784 :
	 *              http://apps.dytech.com.au/jira/browse/TLE-784
	 */
	protected Query addExtraQuery(@Nullable Query query, Search searchreq, IndexReader reader)
	{
		FreeTextQuery fullftQuery = searchreq.getFreeTextQuery();
		if( fullftQuery == null )
		{
			return query;
		}
		BooleanClause clause = convertToBooleanClause(fullftQuery, reader);
		if( !clause.isProhibited() && !clause.isRequired() )
		{
			clause.setOccur(Occur.MUST);
		}

		BooleanQuery andThem = new BooleanQuery();
		andThem.add(clause);
		if( query != null )
		{
			andThem.add(query, Occur.MUST);
		}
		return andThem;
	}

	/**
	 * Takes a FreeTextQuery and converts it to a BooleanClause by dispatching
	 * to the correct implementation.
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	@Nullable
	private BooleanClause convertToBooleanClause(FreeTextQuery query, IndexReader reader)
	{
		if( query instanceof FreeTextFieldQuery )
		{
			return convertField((FreeTextFieldQuery) query, reader);
		}
		else if( query instanceof FreeTextFieldExistsQuery )
		{
			return convertFieldExists((FreeTextFieldExistsQuery) query, reader);
		}
		else if( query instanceof FreeTextDateQuery )
		{
			return convertDate((FreeTextDateQuery) query);
		}
		else if( query instanceof FreeTextAutocompleteQuery )
		{
			return convertAutoComplete((FreeTextAutocompleteQuery) query, reader);
		}
		else
		{
			return convertBoolean((FreeTextBooleanQuery) query, reader);
		}
	}

	private BooleanClause convertFieldExists(FreeTextFieldExistsQuery query, IndexReader reader)
	{
		FreeTextBooleanQuery bquery = new FreeTextBooleanQuery(false, true);
		bquery.add(new FreeTextFieldQuery(FreeTextQuery.FIELD_ALL, query.getField()));
		return convertBoolean(bquery, reader);
	}

	/**
	 * Converts a FreeTextFieldQuery to a BooleanClause
	 * 
	 * @throws ParseException
	 */
	private BooleanClause convertField(FreeTextFieldQuery query, IndexReader reader)
	{
		Query luceneQuery = null;
		if( query.isMustExist() )
		{
			FreeTextBooleanQuery bquery = new FreeTextBooleanQuery(false, true);
			query.setMustExist(false);
			bquery.add(query);
			if( query.getField().charAt(0) == '/' )
			{
				bquery.add(new FreeTextFieldQuery(FreeTextQuery.FIELD_ALL, query.getField()));
			}
			return convertBoolean(bquery, reader);
		}
		if( query.isTokenise() )
		{
			String q = query.getField() + "*";
			try
			{
				luceneQuery = new QueryParser(LuceneConstants.LATEST_VERSION, q, getAnalyser()).parse(query.getValue());
			}
			catch( ParseException e )
			{
				LOGGER.warn("Error parsing query: " + q);
				throw new InvalidSearchQueryException("Error parsing query");
			}
		}
		else if( query.isPossibleWildcard() )
		{
			luceneQuery = new WildcardQuery(new Term(query.getField(), query.getValue()));
		}
		else
		{
			luceneQuery = new TermQuery(new Term(query.getField(), query.getValue()));
		}
		return new BooleanClause(luceneQuery, Occur.SHOULD);
	}

	/**
	 * Converts a FreeTextDateQuery to a BooleanClause
	 */
	private BooleanClause convertDate(FreeTextDateQuery query)
	{
		TermRangeQuery termQuery = new TermRangeQuery(query.getField(), convertDate(query.getStart(), query),
			convertDate(query.getEnd(), query), query.isIncludeStart(), query.isIncludeEnd());
		termQuery.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_FILTER_REWRITE);
		return new BooleanClause(termQuery, Occur.SHOULD);
	}

	@Nullable
	private String convertDate(@Nullable TleDate date, FreeTextDateQuery dateQuery)
	{
		if( date == null )
		{
			return null;
		}

		Dates dateFormat = dateQuery.getDateFormat();
		if( date.isConceptual() )
		{
			return date.getConceptualValue();
		}
		// Probs not required...
		TimeZone timeZone = dateQuery.getTimeZone();
		if( timeZone != null )
		{
			return new LocalDate(date, timeZone).format(dateFormat);
		}
		return date.format(dateFormat);
	}

	/**
	 * Converts a FreeTextBooleanQuery to a BooleanClause
	 */
	private BooleanClause convertBoolean(FreeTextBooleanQuery query, IndexReader reader)
	{
		List<BooleanClause> lclauses = new ArrayList<BooleanClause>();

		List<FreeTextQuery> clauses = query.getClauses();
		boolean and = query.isAnd() && clauses.size() > 1;
		boolean not = query.isNot();

		for( FreeTextQuery clause : clauses )
		{
			BooleanClause bclause = convertToBooleanClause(clause, reader);
			if( bclause != null )
			{
				lclauses.add(bclause);
			}
		}

		BooleanQuery bquery = new BooleanQuery();
		BooleanClause theclause = new BooleanClause(bquery, Occur.SHOULD);
		boolean bprohib = false;
		boolean brequired = false;
		boolean addplus = false;
		if( and )
		{
			addplus = true;
			bprohib = not;
		}
		else if( not )
		{
			bprohib = true;
		}

		boolean allnot = true;
		if( lclauses.size() == 1 )
		{
			BooleanClause first = lclauses.get(0);
			if( !not )
			{
				return first;
			}
			else if( !(first.isRequired() || first.isProhibited()) )
			{
				first.setOccur(Occur.MUST_NOT);
				return first;
			}
		}

		for( BooleanClause bclause : lclauses )
		{
			if( addplus && !(bclause.isRequired() || bclause.isProhibited()) )
			{
				bclause.setOccur(Occur.MUST);
			}
			allnot &= bclause.isProhibited();
			bquery.add(bclause);
		}

		if( allnot )
		{
			bquery.add(new BooleanClause(new TermQuery(new Term(FreeTextQuery.FIELD_ALL, "1")), //$NON-NLS-1$
				Occur.SHOULD));
		}
		if( bprohib )
		{
			theclause.setOccur(Occur.MUST_NOT);
		}
		else if( brequired )
		{
			theclause.setOccur(Occur.MUST);
		}
		else
		{
			theclause.setOccur(Occur.SHOULD);
		}
		return theclause;
	}

	/**
	 * Stops the MultiPhraseQuery from being re-written as a BooleanQuery which
	 * could potentially hit the BooleanQuery.maxClauseCount of 1024
	 */
	private MultiPhraseQuery getMultiPhrase()
	{
		MultiPhraseQuery parsed = new MultiPhraseQuery()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Query rewrite(IndexReader reader)
			{
				return this;
			}
		};
		return parsed;
	}

	// Uses PrefixQuery for a single term
	private BooleanClause convertAutoComplete(FreeTextAutocompleteQuery query, IndexReader reader)
	{
		Query finished = null;
		String raw = query.getQuery();

		List<String> termList = Lists.newArrayList();
		List<Integer> increments = Lists.newArrayList();

		try( TokenStream buffer = getAnalyser().reusableTokenStream(FreeTextQuery.FIELD_NAME_AUTOCOMPLETE,
			new StringReader(raw)) )
		{
			buffer.reset();

			CharTermAttribute termAtt = buffer.getAttribute(CharTermAttribute.class);
			PositionIncrementAttribute posIncrAtt = buffer.getAttribute(PositionIncrementAttribute.class);

			if( termAtt != null )
			{
				while( buffer.incrementToken() )
				{
					termList.add(termAtt.toString());
					increments.add(posIncrAtt.getPositionIncrement());
				}
			}
		}
		catch( IOException ex )
		{
			throw new RuntimeApplicationException("Error reading auto-complete results from search index");
		}

		try
		{
			int termListSize = termList.size();
			if( termListSize == 1 )
			{
				String firstTerm = termList.get(0);
				finished = new PrefixQuery(new Term(FreeTextQuery.FIELD_NAME_AUTOCOMPLETE, firstTerm));
			}
			else
			{
				MultiPhraseQuery multiPhraseQuery = getMultiPhrase();
				int count = -1;
				for( int i = 0; i < termListSize; i++ )
				{
					count += increments.get(i);
					if( i < termListSize - 1 )
					{
						multiPhraseQuery.add(
							new Term[]{new Term(FreeTextQuery.FIELD_NAME_AUTOCOMPLETE, termList.get(i))}, count);
					}
				}

				// Expand prefix
				addExpandedTerms(reader, multiPhraseQuery, termListSize > 0 ? termList.get(termListSize - 1) : "",
					count);
				finished = multiPhraseQuery;
			}
			return new BooleanClause(finished, Occur.MUST);
		}
		catch( IOException ex )
		{
			throw new RuntimeApplicationException("Error reading auto-complete results from search index");
		}
	}

	private void addExpandedTerms(IndexReader reader, MultiPhraseQuery query, String lastTerm, int count)
		throws IOException
	{
		final Term[] expandedTerms = expand(reader, FreeTextQuery.FIELD_NAME_AUTOCOMPLETE, lastTerm);
		if( expandedTerms.length > 0 )
		{
			query.add(expandedTerms, count);
		}
		else if( !lastTerm.isEmpty() )
		{
			query.add(new Term[]{new Term(FreeTextQuery.FIELD_NAME_AUTOCOMPLETE, lastTerm)}, count);
		}
	}

	/**
	 * Create a list of term arrays of no larger than 1024 (Default {@link
	 * BooleanQuery.maxClauseCount()} for boolean queries)
	 */
	public Term[] expand(IndexReader ir, String field, String prefix) throws IOException
	{
		if( prefix.isEmpty() )
		{
			return new Term[0];
		}

		ArrayList<Term> terms = Lists.newArrayList();
		try( TermEnum t = ir.terms(new Term(field, prefix)) )
		{
			do
			{
				if( t.term().text().startsWith(prefix) )
				{
					terms.add(t.term());
				}
				else
				{
					break;
				}
			}
			while( t.next() );
		}
		return terms.toArray(new Term[terms.size()]);
	}

	public void indexBatch(final Collection<IndexedItem> batch)
	{
		modifyIndex(new IndexBuilder()
		{
			@Override
			public long buildIndex(NRTManager nrtManager) throws Exception
			{
				long generation = -1;
				generation = Math.max(generation, removeDocuments(batch, nrtManager));
				generation = Math.max(generation, addDocuments(batch, nrtManager));
				return generation;
			}
		});
	}

	public Operator getDefaultOperator() throws ParseException
	{
		Operator o;

		if( this.defaultOperator.equalsIgnoreCase("and") )
		{
			o = Operator.AND;
		}
		else if( this.defaultOperator.equalsIgnoreCase("OR") )
		{
			o = Operator.OR;
		}
		else
		{
			throw new ParseException("Invalid Default Operator (AND/OR)");
		}

		return o;
	}

	public void setDefaultOperator(String defaultOperator)
	{
		this.defaultOperator = defaultOperator;
	}

	private static class CountingCollector extends Collector
	{
		private int count = 0;

		public int getCount()
		{
			return count;
		}

		@Override
		public void collect(int doc)
		{
			/*
			 * The original HitCollector that counted the docs used a bitset to
			 * the docs, then returned the cardinality. According to Lucence,
			 * <code>collect</code> is only supposed to be called once per
			 * document, so it should be safe to just increment the count for
			 * each invocation. If you start getting counts that are too high,
			 * this may have to change back to using BitSet + cardinality, but
			 * it's probably a bug in Lucene that needs further investigation
			 * anyway.
			 */
			count++;
		}

		@Override
		public boolean acceptsDocsOutOfOrder()
		{
			return true;
		}

		@Override
		public void setScorer(Scorer scorer) throws IOException
		{
			// Nothing to do
		}

		@Override
		public void setNextReader(IndexReader reader, int docBase) throws IOException
		{
			// Nothing to do
		}
	}

	private static class BitSetCollector extends Collector
	{
		private int docBase;
		private final OpenBitSet bitSet = new OpenBitSet();

		public OpenBitSet getBitSet()
		{
			return bitSet;
		}

		@Override
		public void collect(int doc)
		{
			bitSet.set(docBase + doc);
		}

		@Override
		public boolean acceptsDocsOutOfOrder()
		{
			return true;
		}

		@Override
		public void setScorer(Scorer scorer) throws IOException
		{
			// Nothing to do
		}

		@Override
		public void setNextReader(IndexReader reader, int docBase) throws IOException
		{
			this.docBase = docBase;
		}
	}

	public void deleteForInstitution(final long id)
	{
		modifyIndex(new IndexBuilder()
		{
			@Override
			public long buildIndex(NRTManager nrtManager) throws Exception
			{
				nrtManager.deleteDocuments(new Term(FreeTextQuery.FIELD_INSTITUTION, Long.toString(id)));
				return -1;
			}
		});
	}

	@Override
	protected Map<String, Analyzer> getAnalyzerFieldMap(Analyzer autoComplete, Analyzer nonStemmed)
	{
		return ImmutableMap.of(FreeTextQuery.FIELD_BODY_NOSTEM, nonStemmed, FreeTextQuery.FIELD_NAME_AUTOCOMPLETE,
			autoComplete, FreeTextQuery.FIELD_NAME_VECTORED_NOSTEM, nonStemmed, FreeTextQuery.FIELD_BOOKMARK_TAGS,
			nonStemmed, FreeTextQuery.FIELD_ATTACHMENT_VECTORED_NOSTEM, nonStemmed);
	}

	@Override
	public void checkHealth()
	{
		search(new Searcher<T>()
		{

			@Override
			public T search(IndexSearcher searcher) throws IOException
			{
				searcher.getIndexReader().terms(new Term(FreeTextQuery.FIELD_ALL)).term();
				return null;
			}
		});
	}
}
