/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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
import com.tle.core.freetext.filters.CustomFilter;
import com.tle.core.freetext.filters.DateFilter;
import com.tle.core.freetext.filters.InstitutionFilter;
import com.tle.core.freetext.filters.MatrixFilter;
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
import com.tle.freetext.TLEAnalyzer;
import com.tle.freetext.TLEQueryParser;
import it.uniroma3.mat.extendedset.intset.ConciseSet;
import it.uniroma3.mat.extendedset.intset.FastSet;
import it.uniroma3.mat.extendedset.wrappers.LongSet;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.AutomatonTermsEnum;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiTerms;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.TermsEnum.SeekStatus;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollectorManager;
import org.apache.lucene.search.TopScoreDocCollectorManager;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.FixedBitSet;
import org.apache.lucene.util.automaton.CompiledAutomaton;

@SuppressWarnings("nls")
@NonNullByDefault
public abstract class ItemIndex<T extends FreetextResult> extends AbstractIndexEngine {

  private static final Pattern AND = Pattern.compile("(\\W)and(\\W)"); // $NON-NLS-1$
  private static final Pattern OR = Pattern.compile("(\\W)or(\\W)"); // $NON-NLS-1$
  private static final Pattern NOT = Pattern.compile("(\\W)not(\\W)"); // $NON-NLS-1$

  private static final Map<String, String> PRIV_MAP =
      ImmutableMap.of(
          "MODERATE_ITEM",
          "ACLM-",
          "VIEW_ITEM",
          "ACLV-",
          "DISCOVER_ITEM",
          "ACLD-",
          "DELETE_ITEM",
          "ACLR-",
          "DOWNLOAD_ITEM",
          "ACLL-");

  protected FreetextIndex freetextIndex;

  private float titleBoost;
  private float descriptionBoost;
  private float attachmentBoost;

  /** AND or OR */
  private String defaultOperator;

  private StoredFieldVisitor keyFieldSelector;

  public ItemIndex(FreetextIndex freetextIndex) {
    this.freetextIndex = freetextIndex;
  }

  @PostConstruct
  @Override
  public void afterPropertiesSet() throws IOException {
    setStopWordsFile(freetextIndex.getStopWordsFile());
    setDefaultOperator(freetextIndex.getDefaultOperator());
    setAnalyzerLanguage(freetextIndex.getAnalyzerLanguage());
    keyFieldSelector = new DocumentStoredFieldVisitor(getKeyFields());
    super.afterPropertiesSet();
  }

  private float getRealBoostValue(int savedBoostValue) {
    switch (savedBoostValue) {
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
      default:
        return 1;
    }
  }

  private void configureBoost(float boostValue, Consumer<Float> setter, Runnable onZero) {
    if (boostValue == 0) {
      onZero.run();
    }
    setter.accept(boostValue);
  }

  protected Set<String> getKeyFields() {
    return new HashSet<String>(Arrays.asList(FreeTextQuery.FIELD_UNIQUE, FreeTextQuery.FIELD_ID));
  }

  public void setTitleBoost(float titleBoost) {
    this.titleBoost = titleBoost;
  }

  public void setDescriptionBoost(float descriptionBoost) {
    this.descriptionBoost = descriptionBoost;
  }

  public void setAttachmentBoost(float attachmentBoost) {
    this.attachmentBoost = attachmentBoost;
  }

  protected long removeDocuments(Collection<IndexedItem> documents, IndexWriter writer) {
    long generation = -1;
    for (IndexedItem item : documents) {
      ItemIdKey itemId = item.getItemIdKey();
      String unique = ItemId.fromKey(itemId).toString();
      try {
        Builder delQueryBuilder = new Builder();
        delQueryBuilder.add(
            new TermQuery(new Term(FreeTextQuery.FIELD_ID, Long.toString(itemId.getKey()))),
            Occur.MUST);
        delQueryBuilder.add(
            new TermQuery(
                new Term(
                    FreeTextQuery.FIELD_INSTITUTION,
                    Long.toString(item.getInstitution().getUniqueId()))),
            Occur.MUST);
        long g = writer.deleteDocuments(delQueryBuilder.build());
        if (item.isNewSearcherRequired()) {
          generation = g;
        }
      } catch (IOException e) {
        LOGGER.error(
            "An error occurred while attempting to delete item " + unique, e); // $NON-NLS-1$
        throw new RuntimeException(e);
      }
    }
    return generation;
  }

  public long addDocuments(Collection<IndexedItem> documents, IndexWriter writer) {
    long generation = -1;
    for (IndexedItem item : documents) {
      if (item.isAdd()) {
        synchronized (item) {
          Document originalDoc = item.getItemdoc();
          if (originalDoc.getFields().isEmpty()) {
            LOGGER.error("Trying to add an empty document for item:" + item.getId());
            continue;
          }
          try {
            boolean isDocValid = originalDoc.getFields().stream().noneMatch(this::isOversizedTerm);
            Document doc = isDocValid ? originalDoc : dropOversizedFields(originalDoc);
            long g = writer.addDocument(doc);
            if (item.isNewSearcherRequired()) {
              generation = g;
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    return generation;
  }

  // Check if the value of an IndexableField is oversized.
  private boolean isOversizedTerm(IndexableField field) {
    if (field.fieldType().tokenized()) {
      // Always `false` if the field is tokenized, because:
      // 1. Although the value is quite big, it is split into individual terms;
      // 2. The term length limit only applies to individual terms, not the entire value.
      return false;
    }

    String strVal = field.stringValue();
    if (strVal != null) {
      return strVal.getBytes(StandardCharsets.UTF_8).length > IndexWriter.MAX_TERM_LENGTH;
    }

    BytesRef binVal = field.binaryValue();
    if (binVal != null) {
      return binVal.length > IndexWriter.MAX_TERM_LENGTH;
    }

    return false;
  }

  // Makes a copy of the supplied document without the fields that are too big to be indexed.
  private Document dropOversizedFields(Document original) {
    Document copy = new Document();
    original.getFields().stream()
        .filter(
            f -> {
              if (isOversizedTerm(f)) {
                String itemId = original.get(FreeTextQuery.FIELD_UNIQUE);
                LOGGER.warn(
                    "Skip indexing field {} for Item {} because the value exceeds the maximum"
                        + " length",
                    f.name(),
                    itemId);
                return false;
              }

              return true;
            })
        .forEach(copy::add);

    return copy;
  }

  /**
   * Performs a search and returns the results for the given page.
   *
   * @param requestedStart 0 or greater.
   * @param requestedCount 0 or greater. Less than one returns all results.
   */
  public SearchResults<T> search(
      final Search searchreq,
      final int requestedStart,
      final int requestedCount,
      final boolean searchAttachment) {
    return search(
        new Searcher<SearchResults<T>>() {
          @Override
          public SearchResults<T> search(IndexSearcher searcher) throws IOException {
            long t1 = System.currentTimeMillis();

            Sort sorter = getSorter(searchreq);

            // TODO: We should really be doing a
            // Preconditions.checkArgument(requestedStart >= 0) and the same
            // for requestedCount too rather than trying to just work.
            int actualStart = Math.max(0, requestedStart);
            int actualCount = -1;
            int numDocs = searcher.getIndexReader().numDocs();

            if (requestedCount < 0
                || requestedCount == Integer.MAX_VALUE
                || requestedCount > numDocs) {
              actualCount = numDocs;
            } else {
              actualCount = requestedCount + actualStart;
              if (actualCount > numDocs) {
                actualCount = numDocs;
              }
            }

            SearchResults<T> results;
            Query query = getQuery(searchreq, searcher.getIndexReader(), searchAttachment);
            boolean searchAll =
                Check.isEmpty(searchreq.getQuery()) || searchreq.getQuery().equals("*");

            boolean sortByRelevance =
                Optional.ofNullable(searchreq.getSortFields())
                    .map(Arrays::stream)
                    .flatMap(fields -> fields.filter(f -> f.getType() == Type.SCORE).findFirst())
                    .isPresent();

            if (actualCount == 0) {
              TotalHitCountCollector hitCount = new TotalHitCountCollector();
              searcher.search(query, hitCount);
              results =
                  new SimpleSearchResults<T>(new ArrayList<T>(), 0, 0, hitCount.getTotalHits());
            } else {
              CollectorManager<? extends Collector, ? extends TopDocs> itemCollectorManager =
                  sortByRelevance
                      ? new TopScoreDocCollectorManager(actualCount, Integer.MAX_VALUE)
                      : new TopFieldCollectorManager(sorter, actualCount, Integer.MAX_VALUE);

              TopDocs hits = searcher.search(query, itemCollectorManager);

              final SearchResults<T> itemResults =
                  getResultsFromTopDocs(searcher, hits, actualStart, sortByRelevance);

              SearchResults<T> attachmentResults = null;
              int attachmentBoostValue = freetextIndex.getSearchSettings().getAttachmentBoost();
              if (searchAll) {
                // don't worry about attachment relevance
                results = itemResults;
              } else if (attachmentBoostValue != 0 && searchAttachment) {
                final String[] fields = new String[] {FreeTextQuery.FIELD_ATTACHMENT_VECTORED};

                Query queryAttachmentOnly =
                    getQuery(searchreq, searcher.getIndexReader(), fields, searchAttachment);
                queryAttachmentOnly =
                    addUniqueIdClauseToQuery(
                        queryAttachmentOnly, itemResults, searcher.getIndexReader());

                // Attachments do not care scores so always use TopFieldCollectorManager.
                CollectorManager<? extends Collector, ? extends TopDocs>
                    attachmentCollectorManager =
                        new TopFieldCollectorManager(sorter, actualCount, Integer.MAX_VALUE);

                final TopDocs attachmentHits =
                    searcher.search(queryAttachmentOnly, attachmentCollectorManager);
                attachmentResults =
                    getResultsFromTopDocs(searcher, attachmentHits, 0, sortByRelevance);
              }

              if (attachmentResults != null) {
                results = markItemKeywordFoundInAttachment(itemResults, attachmentResults);
              } else {
                results = itemResults;
              }
            }
            long t2 = System.currentTimeMillis();

            if (!(searchreq.getFreeTextQuery() instanceof FreeTextAutocompleteQuery)) {
              LOGGER.info(
                  "Query["
                      + query
                      + "] Hits["
                      + results.getAvailable()
                      + "] Returning["
                      + results.getCount()
                      + "] Time Elapsed["
                      + (t2 - t1)
                      + "ms] Total Indexed["
                      + numDocs
                      + "]");
            }

            return results;
          }
        });
  }

  private Query addUniqueIdClauseToQuery(
      Query query, SearchResults<T> itemResults, IndexReader reader) {
    List<T> results = itemResults.getResults();
    Builder freeTextBuilder = new Builder();
    for (T t : results) {
      ItemIdKey itemIdKey = t.getItemIdKey();
      String[] split = itemIdKey.toString().split("/");
      String uniqueId = split[1] + "/" + split[2];

      FreeTextBooleanQuery bquery = new FreeTextBooleanQuery(false, true);
      bquery.add(new FreeTextFieldQuery(FreeTextQuery.FIELD_UNIQUE, uniqueId));
      BooleanClause clause = convertToBooleanClause(bquery, reader);
      freeTextBuilder.add(clause.getQuery(), Occur.SHOULD);
    }

    Builder fullQueryBuilder = new Builder();
    fullQueryBuilder.add(query, Occur.MUST);
    fullQueryBuilder.add(freeTextBuilder.build(), Occur.MUST);

    return fullQueryBuilder.build();
  }

  private SearchResults<T> markItemKeywordFoundInAttachment(
      SearchResults<T> results, SearchResults<T> attachmentResults) {
    List<T> attachments = attachmentResults.getResults();

    for (int i = 0; i < results.getResults().size(); i++) {
      T t = results.getResults().get(i);
      t.setKeywordFoundInAttachment(false);

      for (T attachment : attachments) {
        if (t.getItemIdKey().equals(attachment.getItemIdKey())) {
          t.setKeywordFoundInAttachment(true);
          break;
        }
      }
    }
    return results;
  }

  public LongSet searchBitSet(final Search searchreq, final boolean searchAttachments) {
    return search(
        new Searcher<LongSet>() {
          @Override
          public LongSet search(IndexSearcher searcher) throws IOException {
            long t1 = System.currentTimeMillis();

            IndexReader indexReader = searcher.getIndexReader();
            Query query = getQuery(searchreq, indexReader, searchAttachments);
            int numDocs = indexReader.numDocs();

            CompressedSetCollector compCollector = new CompressedSetCollector();
            searcher.search(query, compCollector);
            LongSet docIdSet = compCollector.getSet();

            LongSet longSet = new LongSet(new FastSet());
            Iterator<Long> iterator = docIdSet.iterator();
            while (iterator.hasNext()) {
              long doc = iterator.next();
              Document document =
                  indexReader.document((int) doc, Collections.singleton(FreeTextQuery.FIELD_ID));
              long key = Long.parseLong(document.get(FreeTextQuery.FIELD_ID));
              longSet.add(key);
            }
            LongSet results = new LongSet(new ConciseSet());
            results.addAll(longSet);

            long t2 = System.currentTimeMillis();
            long size = results.size();
            LOGGER.info(
                "Query["
                    + query
                    + "] Hits["
                    + size
                    + "] Returning["
                    + size
                    + "] Time Elapsed["
                    + (t2 - t1)
                    + "ms] Total Indexed["
                    + numDocs
                    + "]");

            return results;
          }
        });
  }

  public static String convertStdPriv(String priv) {
    String pfx = PRIV_MAP.get(priv);
    if (pfx == null) {
      throw new Error("Unknown prefix for:" + priv); // $NON-NLS-1$
    }
    return pfx;
  }

  protected String getPrefixForPrivilege(String priv) {
    return convertStdPriv(priv);
  }

  protected LongSet getBitSetFromTopDocs(IndexSearcher searcher, TopDocs hits, int firstHit)
      throws IOException {
    LongSet longSet = new LongSet(new ConciseSet());
    ScoreDoc[] results = hits.scoreDocs;
    if (firstHit < results.length) {
      for (int i = firstHit; i < results.length; i++) {
        int docId = results[i].doc;
        Document doc = searcher.doc(docId, getKeyFields());
        ItemIdKey key = getKeyForDocument(doc);
        longSet.add(key.getKey());
      }
    }
    return longSet;
  }

  protected SearchResults<T> getResultsFromTopDocs(
      IndexSearcher searcher, TopDocs hits, int firstHit, boolean sortByRelevance)
      throws IOException {
    List<T> retrievedResults = new ArrayList<T>();

    ScoreDoc[] results = hits.scoreDocs;
    if (firstHit < results.length) {
      for (int i = firstHit; i < results.length; i++) {
        int docId = results[i].doc;
        float relevance = results[i].score;
        Document doc = searcher.doc(docId, getKeyFields());
        ItemIdKey key = getKeyForDocument(doc);
        T result = createResult(key, doc, relevance, sortByRelevance);
        retrievedResults.add(result);
      }
    }

    // Accoding to Lucene V7 doco, TopDocs instances returned by IndexSearcher will still have a
    // total number of hits which is less than 2B since Lucene indexes are still bound to at most
    // 2B documents, so it can safely be cast to an int in that case.
    return new SimpleSearchResults<T>(
        retrievedResults, retrievedResults.size(), firstHit, (int) hits.totalHits.value);
  }

  protected abstract T createResult(
      ItemIdKey key, Document doc, float relevance, boolean sortByRelevance);

  protected ItemIdKey getKeyForDocument(Document doc) {
    String id = doc.get(FreeTextQuery.FIELD_UNIQUE);
    long key = Long.parseLong(doc.get(FreeTextQuery.FIELD_ID));
    return new ItemIdKey(key, new ItemId(id));
  }

  /**
   * Counts the number of documents that a query matches without actually retrieving the documents.
   * Should be pretty damn fast.
   */
  public int count(final Search searchreq, final boolean isSearchAttachment) {
    return search(
        new Searcher<Integer>() {
          @Override
          public Integer search(IndexSearcher searcher) throws IOException {
            Query query = getQuery(searchreq, searcher.getIndexReader(), isSearchAttachment);

            if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("Counting for " + query);
            }

            CountingCollector collector = new CountingCollector();
            searcher.search(query, collector);

            int count = collector.getCount();
            if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("Counted " + count + " items");
            }
            return count;
          }
        });
  }

  /**
   * A simplified implementation of matrixSearch() that only works on a single field, and currently
   * only returns the count per term. It could easily be extended to return a list of ItemIds per
   * term, it simply wasn't necessary when I was writing it!
   *
   * <p>This simplified implementation was written to overcome the memory pressures that
   * matrixSearch() creates when you have over half a million terms for a field. MatrixSearch()
   * creates *many* BitSets that it holds on to to reuse as it recurse through a list of fields.
   * Since we only care about a single field in this implementation, we can avoid generating and
   * holding onto BitSets.
   */
  public Multimap<String, Pair<String, Integer>> facetCount(
      @Nullable final Search searchreq, final Collection<String> fields) {
    return search(
        new Searcher<Multimap<String, Pair<String, Integer>>>() {
          @Override
          public Multimap<String, Pair<String, Integer>> search(IndexSearcher searcher)
              throws IOException {
            final IndexReader reader = searcher.getIndexReader();
            final FixedBitSet filteredBits =
                searchRequestToBitSet(searchreq, searcher, reader, false);

            final Multimap<String, Pair<String, Integer>> rv = ArrayListMultimap.create();
            for (String field : fields) {
              for (Term term : new XPathFieldIterator(reader, field)) {
                PostingsEnum docs = MultiTerms.getTermPostingsEnum(reader, field, term.bytes());

                LuceneDocumentHelper.useDocCount(
                    docs,
                    filteredBits,
                    count -> {
                      if (count > 0) {
                        rv.put(field, new Pair<>(term.text(), count));
                      }
                    });
              }
            }
            return rv;
          }
        });
  }

  public MatrixResults matrixSearch(
      @Nullable final Search searchreq,
      final List<String> fields,
      final boolean countOnly,
      final boolean searchAttachments) {
    return search(
        new Searcher<MatrixResults>() {
          @Override
          public MatrixResults search(IndexSearcher searcher) throws IOException {
            IndexReader reader = searcher.getIndexReader();
            FixedBitSet filteredBits =
                searchRequestToBitSet(searchreq, searcher, reader, searchAttachments);
            int maxDoc = reader.maxDoc();

            Map<String, Map<String, List<TermBitSet>>> xpathMap =
                new HashMap<String, Map<String, List<TermBitSet>>>();

            List<FixedBitSet> perFieldBitSets = new ArrayList<FixedBitSet>();
            FixedBitSet allDocs = new FixedBitSet(reader.maxDoc());
            for (String field : fields) {
              boolean hasXpaths = field.indexOf('[') != -1;
              FixedBitSet perFieldBitSet = new FixedBitSet(maxDoc);
              for (Term term : new XPathFieldIterator(reader, field)) {
                FixedBitSet set = new FixedBitSet(maxDoc);
                PostingsEnum docs = MultiTerms.getTermPostingsEnum(reader, field, term.bytes());
                LuceneDocumentHelper.forEachDoc(docs, set::set);

                perFieldBitSet.or(set);
                allDocs.or(set);
                String xpathKey = "";
                if (hasXpaths) {
                  String fieldName = term.field();
                  int ind = fieldName.lastIndexOf(']');
                  if (ind != -1) {
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
            for (FixedBitSet set : perFieldBitSets) {
              set.xor(allDocs);
            }

            MatrixResults results = new MatrixResults();
            results.setFields(fields);

            Map<String, List<TermBitSet>> blankPaths = xpathMap.get("");
            for (Map<String, List<TermBitSet>> map : xpathMap.values()) {
              List<List<TermBitSet>> fieldsToRecurse = new ArrayList<List<TermBitSet>>();
              for (String field : fields) {
                List<TermBitSet> list = map.get(field);
                if (list == null) {
                  list = blankPaths.get(field);
                }
                fieldsToRecurse.add(list);
              }
              recurseTerms(
                  fieldsToRecurse,
                  0,
                  new String[fields.size()],
                  filteredBits,
                  results,
                  reader,
                  countOnly);
            }
            return results;
          }
        });
  }

  private FixedBitSet searchRequestToBitSet(
      @Nullable final Search searchreq,
      IndexSearcher searcher,
      IndexReader reader,
      boolean searchAttachments)
      throws IOException {
    Builder builder = new Builder();
    Query query;
    if (searchreq != null) {
      query = getQuery(searchreq, reader, searchAttachments);
    } else {
      InstitutionFilter institutionFilter = new InstitutionFilter();
      builder.add(institutionFilter.buildQuery(), institutionFilter.getOccur());
      query = builder.build();
    }

    BitSetCollector collector = new BitSetCollector(reader.maxDoc());
    searcher.search(query, collector);
    return collector.getBitSet();
  }

  public String suggestTerm(
      final Search request, final String prefix, final boolean isSearchAttachment) {
    return search(
        new Searcher<String>() {
          @Override
          public String search(IndexSearcher searcher) throws IOException {
            // Get the reader
            IndexReader reader = searcher.getIndexReader();
            Query query = getQuery(request, reader, isSearchAttachment);
            BitSetCollector collector = new BitSetCollector(reader.maxDoc());
            searcher.search(query, collector);

            // This is all the docs that are permitted by the filters.
            FixedBitSet permittedDocSet = collector.getBitSet();

            // Get docs that contain terms that begin with the prefix
            List<Term> termList = Lists.newArrayList();
            termList.add(new Term(FreeTextQuery.FIELD_BODY_NOSTEM, prefix));
            termList.add(new Term(FreeTextQuery.FIELD_ATTACHMENT_VECTORED_NOSTEM, prefix));

            for (Term term : termList) {
              // Given a field, find out all the terms and seek the terms that have the provided
              // prefix so the result
              // does not have to be the exact term.
              TermsEnum termsEnum = MultiTerms.getTerms(reader, term.field()).iterator();
              if (termsEnum.seekCeil(new BytesRef(prefix)) != SeekStatus.END) {
                Stream<Integer> docIds =
                    LuceneDocumentHelper.postingEnumToStream(termsEnum.postings(null));
                // Check if any of the documents with the term are 'permitted' to the user
                if (docIds.anyMatch(permittedDocSet::get)) {
                  // If so, return that as the term to use
                  return termsEnum.term().utf8ToString();
                }
              }
            }
            return "";
          }
        });
  }

  private void addFieldBitSet(
      Term term,
      FixedBitSet set,
      String xpathKey,
      Map<String, Map<String, List<TermBitSet>>> xpathMap,
      String field) {
    Map<String, List<TermBitSet>> map = xpathMap.get(xpathKey);
    if (map == null) {
      map = new HashMap<String, List<TermBitSet>>();
      xpathMap.put(xpathKey, map);
    }
    List<TermBitSet> list = map.get(field);
    if (list == null) {
      list = new ArrayList<TermBitSet>();
      map.put(field, list);
    }
    list.add(new TermBitSet(term, set));
  }

  private final class ItemIdFieldSelector extends StoredFieldVisitor {

    @Override
    public Status needsField(FieldInfo fieldInfo) {
      return FreeTextQuery.FIELD_ID.equals(fieldInfo.name) ? Status.YES : Status.NO;
    }
  }

  public static class TermBitSet {

    Term term;
    FixedBitSet bitSet;

    public TermBitSet(Term term, FixedBitSet bitSet) {
      this.term = term;
      this.bitSet = bitSet;
    }
  }

  private void recurseTerms(
      List<List<TermBitSet>> bitSets,
      int index,
      String[] curValues,
      FixedBitSet curBits,
      MatrixResults results,
      IndexReader reader,
      boolean countOnly) {
    List<TermBitSet> termBitSetList = bitSets.get(index);
    boolean last = index == curValues.length - 1;
    for (TermBitSet termBitSet : termBitSetList) {
      FixedBitSet termBits = termBitSet.bitSet;
      Term term = termBitSet.term;
      // if we don't intersect there's no point in recursing further in
      if (curBits.intersects(termBits)) {
        // Collect current term's value into the value array
        curValues[index] = term.text();
        FixedBitSet docBits = (FixedBitSet) curBits.clone();
        docBits.and(termBits);
        if (last) {
          int count;
          List<ItemIdKey> ids = null;
          ArrayList<String> vals = new ArrayList<String>(Arrays.asList(curValues));
          if (!countOnly) {
            ids = getIdsForBitset(docBits, reader);
            count = ids.size();
          } else {
            count = (int) docBits.cardinality();
          }
          results.addEntry(new MatrixResults.MatrixEntry(vals, ids, count));
        } else {
          recurseTerms(bitSets, index + 1, curValues, docBits, results, reader, countOnly);
        }
      }
    }
  }

  private List<ItemIdKey> getIdsForBitset(FixedBitSet docBits, IndexReader reader) {
    int docid = 0;
    List<ItemIdKey> keys = new ArrayList<ItemIdKey>();
    while (true) {
      docid = docBits.nextSetBit(docid);
      if (docid == -1 || docid == PostingsEnum.NO_MORE_DOCS) {
        break;
      }
      Document doc;
      try {
        doc = reader.document(docid);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      ItemIdKey key = getKeyForDocument(doc);
      keys.add(key);
      docid++;
    }
    return keys;
  }

  /** Takes a search request and prepares a Lucene Query object. */
  protected BooleanQuery getQuery(Search request, IndexReader reader, boolean searchAttachment) {
    final String[] fields =
        FreeTextQuery.BASIC_NAME_BODY_ATTACHMENT_FIELDS.toArray(
            new String[FreeTextQuery.BASIC_NAME_BODY_ATTACHMENT_FIELDS.size()]);
    return getQuery(request, reader, fields, searchAttachment);
  }

  private BooleanQuery getQuery(
      Search request, IndexReader reader, String[] allFields, boolean searchAttachment) {
    String[] fields = buildSearchFields(allFields, searchAttachment);

    // This full query builder includes all the general search queries plus the queries for filters.
    Builder fullQuerybuilder = new Builder();
    fullQuerybuilder.add(getFilterQuery(request, reader), Occur.FILTER);

    // This search query builder includes the normal query string and all the extra queries like
    // FreeText query.
    Builder searchQueryBuilder = new Builder();
    searchQueryBuilder.add(buildNormalQuery(request, fields), Occur.MUST);
    Optional.ofNullable(buildExtraQuery(request, reader))
        .ifPresent(q -> searchQueryBuilder.add(q, Occur.MUST));
    fullQuerybuilder.add(searchQueryBuilder.build(), Occur.MUST);

    return fullQuerybuilder.build();
  }

  /**
   * Build a BooleanQuery to represent all the Lucene filters applied in the search in two steps.
   *
   * <p>1. Build a custom Lucene filter for each applied configuration. Each filter is responsible
   * for their equivalent query and occurrence. 2. Build a BooleanQuery to include queries of all
   * the filters.
   *
   * <p>Note that some filters may return `null` as their queries, which means they are actually not
   * applicable. So they will not be added to the final BooleanQuery.
   */
  protected BooleanQuery getFilterQuery(Search request, IndexReader reader) {
    Builder filterQueryBuilder = new Builder();

    List<CustomFilter> filters = Lists.newArrayList();

    Date[] dateRange = request.getDateRange();
    if (dateRange != null) {
      filters.add(
          createDateFilter(
              FreeTextQuery.FIELD_REALLASTMODIFIED,
              dateRange,
              Dates.ISO,
              request.useServerTimeZone()));
    }

    Collection<com.tle.common.searching.DateFilter> dateFilters = request.getDateFilters();
    if (dateFilters != null) {
      for (com.tle.common.searching.DateFilter dateFilter : dateFilters) {
        Date[] range = dateFilter.getRange();
        String indexFieldName = dateFilter.getIndexFieldName();
        if (dateFilter.getFormat() == Format.ISO) {
          filters.add(
              createDateFilter(indexFieldName, range, Dates.ISO, request.useServerTimeZone()));
        } else {
          long start = range[0] != null ? range[0].getTime() : Long.MIN_VALUE;
          long end = range[1] != null ? range[1].getTime() : Long.MAX_VALUE;
          filters.add(() -> LongPoint.newRangeQuery(indexFieldName, start, end));
        }
      }
    }

    String privPrefix = request.getPrivilegePrefix();
    String privilege = request.getPrivilege();
    if (privPrefix == null && privilege != null) {
      privPrefix = getPrefixForPrivilege(privilege);
    }
    if (privPrefix != null) {
      filters.add(new SecurityFilter(privPrefix, reader));
    }

    List<List<Field>> must = request.getMust();
    List<List<Field>> mustNot = request.getMustNot();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Must " + must + ": Must Not: " + mustNot + " Privilege:" + privilege);
    }
    if (must != null && !must.isEmpty()) {
      filters.add(new MustFilter(must));
    }
    if (mustNot != null && !mustNot.isEmpty()) {
      filters.add(new MustNotFilter(mustNot));
    }
    List<Field> matrixFields = request.getMatrixFields();
    if (matrixFields != null) {
      filters.add(new MatrixFilter(matrixFields, reader));
    }
    filters.add(new InstitutionFilter());

    filters.forEach(
        f ->
            Optional.ofNullable(f.buildQuery())
                .ifPresent(q -> filterQueryBuilder.add(q, f.getOccur())));

    return filterQueryBuilder.build();
  }

  private String[] buildSearchFields(String[] allFields, boolean searchAttachment) {
    List<String> searchFields = new ArrayList<>(Arrays.asList(allFields));
    SearchSettings searchSettings = freetextIndex.getSearchSettings();

    configureBoost(
        getRealBoostValue(searchSettings.getTitleBoost()),
        this::setTitleBoost,
        () -> searchFields.remove(FreeTextQuery.FIELD_NAME_VECTORED));

    configureBoost(
        getRealBoostValue(searchSettings.getDescriptionBoost()),
        this::setDescriptionBoost,
        () -> searchFields.remove(FreeTextQuery.FIELD_BODY));

    configureBoost(
        !searchAttachment ? 0 : getRealBoostValue(searchSettings.getAttachmentBoost()),
        this::setAttachmentBoost,
        () -> searchFields.remove(FreeTextQuery.FIELD_ATTACHMENT_VECTORED));

    return searchFields.toArray(new String[0]);
  }

  protected DateFilter createDateFilter(
      String fieldName, Date[] range, Dates indexDateFormat, boolean useServerTimeZone) {
    if (range.length != 2 || (range[0] != null && range[1] != null && range[0].after(range[1]))) {
      throw new InvalidDateRangeException();
    }
    return new DateFilter(
        fieldName,
        range[0],
        range[1],
        indexDateFormat,
        useServerTimeZone ? TimeZone.getDefault() : null);
  }

  /**
   * Takes a search request and prepares a Lucene Sort object, or null if no sorting is required.
   */
  protected Sort getSorter(Search request) {
    com.tle.common.searching.SortField[] sortfields = request.getSortFields();
    if (sortfields != null) {
      SortField[] convFields = new SortField[sortfields.length];
      int i = 0;
      for (com.tle.common.searching.SortField sortfield : sortfields) {
        FieldComparatorSource fieldComparatorSource = null;
        SortField.Type type = SortField.Type.STRING;
        switch (sortfield.getType()) {
          case INT:
            type = SortField.Type.INT;
            break;
          case LONG:
            type = SortField.Type.LONG;
            break;
          case SCORE:
            type = SortField.Type.SCORE;
            break;
          case CUSTOM:
            type = SortField.Type.CUSTOM;
            fieldComparatorSource = sortfield.getFieldComparatorSource();
          default:
            // Stays STRING
            break;
        }
        // @formatter:off
        SortField sortField =
            fieldComparatorSource != null
                ? new SortField(
                    sortfield.getField(), fieldComparatorSource, request.isSortReversed())
                : new SortField(
                    sortfield.getField(), type, sortfield.isReverse() ^ request.isSortReversed());
        // @formatter:on
        convFields[i++] = sortField;
      }
      return new Sort(convFields);
    }
    return new Sort(new SortField(null, SortField.Type.SCORE, false));
  }

  /**
   * Build a BooleanQuery for the normal query string of a search and some extra query strings
   * provided by certain contexts such as tags of a favourite search.
   *
   * <p>There are three steps.
   * <li>Get the text of a normal query string and use {@link TLEQueryParser} to parse it.
   * <li>Get a list of extra query strings and parse them as well.
   * <li>Join all the parsed results by `Occur.SHOULD`.
   */
  private BooleanQuery buildNormalQuery(Search request, String[] fields) {
    String queryString = request.getQuery();
    Builder normalQueryBuilder = new Builder();
    try {
      if (Check.isEmpty(queryString) || queryString.trim().equals("*")) {
        normalQueryBuilder.add(new TermQuery(new Term(FreeTextQuery.FIELD_ALL, "1")), Occur.MUST);
      } else {
        queryString = queryString.toLowerCase();
        queryString = AND.matcher(queryString).replaceAll("$1AND$2");
        queryString = OR.matcher(queryString).replaceAll("$1OR$2");
        queryString = NOT.matcher(queryString).replaceAll("$1NOT$2");

        Map<String, Float> boosts = Maps.newHashMap();
        boosts.put(FreeTextQuery.FIELD_NAME_VECTORED, titleBoost);
        boosts.put(FreeTextQuery.FIELD_BODY, descriptionBoost);
        boosts.put(FreeTextQuery.FIELD_ATTACHMENT_VECTORED, attachmentBoost);

        TLEQueryParser tleParser =
            new TLEQueryParser(fields, getAnalyser(), boosts, getDefaultOperator());
        Query tleQuery = tleParser.parse(queryString);

        normalQueryBuilder.add(tleQuery, Occur.SHOULD);

        List<String> queries = request.getExtraQueries();
        if (queries != null) {
          for (String queryStr : queries) {
            normalQueryBuilder.add(tleParser.parse(queryStr), Occur.SHOULD);
          }
        }
      }

    } catch (QueryNodeException ex) {
      throw new InvalidSearchQueryException(queryString, ex);
    }
    return normalQueryBuilder.build();
  }

  /**
   * Build a BooleanQuery for different implementations of {@link FreeTextQuery} such as
   * `FreeTextAutocompleteQuery` and `FreeTextDateQuery`.
   */
  private BooleanQuery buildExtraQuery(Search searchreq, IndexReader reader) {
    Builder extraQueryBuilder = new Builder();
    FreeTextQuery fullftQuery = searchreq.getFreeTextQuery();
    if (fullftQuery == null) {
      return null;
    }
    BooleanClause clause = convertToBooleanClause(fullftQuery, reader);
    Occur occur = clause.getOccur();
    if (!clause.isProhibited() && !clause.isRequired()) {
      occur = Occur.MUST;
    }
    extraQueryBuilder.add(clause.getQuery(), occur);

    return extraQueryBuilder.build();
  }

  /**
   * Takes a FreeTextQuery and converts it to a BooleanClause by dispatching to the correct
   * implementation.
   */
  @Nullable
  private BooleanClause convertToBooleanClause(FreeTextQuery query, IndexReader reader) {
    if (query instanceof FreeTextFieldQuery) {
      return convertField((FreeTextFieldQuery) query, reader);
    } else if (query instanceof FreeTextFieldExistsQuery) {
      return convertFieldExists((FreeTextFieldExistsQuery) query, reader);
    } else if (query instanceof FreeTextDateQuery) {
      return convertDate((FreeTextDateQuery) query);
    } else if (query instanceof FreeTextAutocompleteQuery) {
      return convertAutoComplete((FreeTextAutocompleteQuery) query, reader);
    } else {
      return convertBoolean((FreeTextBooleanQuery) query, reader);
    }
  }

  private BooleanClause convertFieldExists(FreeTextFieldExistsQuery query, IndexReader reader) {
    FreeTextBooleanQuery bquery = new FreeTextBooleanQuery(false, true);
    bquery.add(new FreeTextFieldQuery(FreeTextQuery.FIELD_ALL, query.getField()));
    return convertBoolean(bquery, reader);
  }

  /** Converts a FreeTextFieldQuery to a BooleanClause */
  private BooleanClause convertField(FreeTextFieldQuery query, IndexReader reader) {
    Query luceneQuery = null;
    if (query.isMustExist()) {
      FreeTextBooleanQuery bquery = new FreeTextBooleanQuery(false, true);
      query.setMustExist(false);
      bquery.add(query);
      if (query.getField().charAt(0) == '/') {
        bquery.add(new FreeTextFieldQuery(FreeTextQuery.FIELD_ALL, query.getField()));
      }
      return convertBoolean(bquery, reader);
    }
    if (query.isTokenise()) {
      String field = query.getField() + "*";
      String value = query.getValue();
      try {
        // Must escape the query when using StandardQueryParser.
        luceneQuery =
            new StandardQueryParser(getAnalyser()).parse(QueryParserUtil.escape(value), field);
      } catch (QueryNodeException e) {
        LOGGER.warn("Error parsing query " + value + " for field " + field);
        throw new InvalidSearchQueryException("Error parsing query");
      }
    } else if (query.isPossibleWildcard()) {
      luceneQuery = new WildcardQuery(new Term(query.getField(), query.getValue()));
    } else {
      luceneQuery = new TermQuery(new Term(query.getField(), query.getValue()));
    }
    return new BooleanClause(luceneQuery, Occur.SHOULD);
  }

  /** Converts a FreeTextDateQuery to a BooleanClause */
  private BooleanClause convertDate(FreeTextDateQuery query) {
    TermRangeQuery termQuery =
        TermRangeQuery.newStringRange(
            query.getField(),
            convertDate(query.getStart(), query),
            convertDate(query.getEnd(), query),
            query.isIncludeStart(),
            query.isIncludeEnd());
    termQuery.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_REWRITE);
    return new BooleanClause(termQuery, Occur.SHOULD);
  }

  @Nullable
  private String convertDate(@Nullable TleDate date, FreeTextDateQuery dateQuery) {
    if (date == null) {
      return null;
    }

    Dates dateFormat = dateQuery.getDateFormat();
    if (date.isConceptual()) {
      return date.getConceptualValue();
    }
    // Probs not required...
    TimeZone timeZone = dateQuery.getTimeZone();
    if (timeZone != null) {
      return new LocalDate(date, timeZone).format(dateFormat);
    }
    return date.format(dateFormat);
  }

  /** Converts a FreeTextBooleanQuery to a BooleanClause */
  private BooleanClause convertBoolean(FreeTextBooleanQuery query, IndexReader reader) {
    Builder queryBuilder = new Builder();
    List<BooleanClause> lclauses = new ArrayList<>();
    List<FreeTextQuery> clauses = query.getClauses();

    boolean and = query.isAnd() && clauses.size() > 1;
    boolean not = query.isNot();

    for (FreeTextQuery clause : clauses) {
      BooleanClause bclause = convertToBooleanClause(clause, reader);
      if (bclause != null) {
        lclauses.add(bclause);
      }
    }

    boolean bprohib = false;
    boolean addplus = false;
    if (and) {
      addplus = true;
      bprohib = not;
    } else if (not) {
      bprohib = true;
    }

    boolean allnot = true;
    if (lclauses.size() == 1) {
      BooleanClause first = lclauses.get(0);
      if (!not) {
        return first;
      } else if (!(first.isRequired() || first.isProhibited())) {
        return new BooleanClause(first.getQuery(), Occur.MUST_NOT);
      }
    }

    for (BooleanClause bclause : lclauses) {
      Occur clauseOccur = bclause.getOccur();
      if (addplus && !(bclause.isRequired() || bclause.isProhibited())) {
        clauseOccur = Occur.MUST;
      }
      allnot &= bclause.isProhibited();
      queryBuilder.add(bclause.getQuery(), clauseOccur);
    }

    // When `allnot` is true, it means all the BooleanClause are prohibited, so their occurrences
    // are MUST_NOT.
    // In this case the query has to have a term that can return some documents, and we use field
    // `ALL` to
    // return all the documents.
    if (allnot) {
      queryBuilder.add(new TermQuery(new Term(FreeTextQuery.FIELD_ALL, "1")), Occur.SHOULD);
    }

    return new BooleanClause(queryBuilder.build(), bprohib ? Occur.MUST_NOT : Occur.SHOULD);
  }

  // Uses PrefixQuery for a single term
  private BooleanClause convertAutoComplete(FreeTextAutocompleteQuery query, IndexReader reader) {
    List<String> termList = Lists.newArrayList();
    List<Integer> increments = Lists.newArrayList();

    TLEAnalyzer autocompleteAnalyzer = getAutoCompleteAnalyzer();

    try (TokenStream buffer =
        autocompleteAnalyzer.tokenStream(
            FreeTextQuery.FIELD_NAME_AUTOCOMPLETE, new StringReader(query.getQuery()))) {
      buffer.reset();

      CharTermAttribute termAtt = buffer.getAttribute(CharTermAttribute.class);
      PositionIncrementAttribute posIncrAtt = buffer.getAttribute(PositionIncrementAttribute.class);
      if (termAtt != null) {
        while (buffer.incrementToken()) {
          termList.add(termAtt.toString());
          increments.add(posIncrAtt.getPositionIncrement());
        }
      }

      buffer.end();
    } catch (IOException ex) {
      throw new RuntimeApplicationException(
          "Error reading auto-complete results from search index");
    }

    try {
      Query finished = null;
      int termListSize = termList.size();
      if (termListSize == 1) {
        String firstTerm = termList.get(0);
        finished = new PrefixQuery(new Term(FreeTextQuery.FIELD_NAME_AUTOCOMPLETE, firstTerm));
      } else {
        MultiPhraseQuery.Builder multiPhraseQueryBuilder = new MultiPhraseQuery.Builder();
        int count = -1;
        for (int i = 0; i < termListSize; i++) {
          count += increments.get(i);
          if (i < termListSize - 1) {
            multiPhraseQueryBuilder.add(
                new Term[] {new Term(FreeTextQuery.FIELD_NAME_AUTOCOMPLETE, termList.get(i))},
                count);
          }
        }

        // Expand prefix
        addExpandedTerms(
            reader,
            multiPhraseQueryBuilder,
            termListSize > 0 ? termList.get(termListSize - 1) : "",
            count);
        finished = multiPhraseQueryBuilder.build();
      }
      return new BooleanClause(finished, Occur.MUST);
    } catch (IOException ex) {
      throw new RuntimeApplicationException(
          "Error reading auto-complete results from search index");
    }
  }

  private void addExpandedTerms(
      IndexReader reader, MultiPhraseQuery.Builder builder, String lastTerm, int count)
      throws IOException {
    final Term[] expandedTerms = expand(reader, FreeTextQuery.FIELD_NAME_AUTOCOMPLETE, lastTerm);
    if (expandedTerms.length > 0) {
      builder.add(expandedTerms, count);
    } else if (!lastTerm.isEmpty()) {
      builder.add(new Term[] {new Term(FreeTextQuery.FIELD_NAME_AUTOCOMPLETE, lastTerm)}, count);
    }
  }

  /**
   * Create a list of term arrays of no larger than 1024 (Default {@link
   * BooleanQuery.maxClauseCount()} for boolean queries)
   */
  public Term[] expand(IndexReader ir, String field, String prefix) throws IOException {
    if (prefix.isEmpty()) {
      return new Term[0];
    }

    ArrayList<Term> terms = Lists.newArrayList();
    TermsEnum termsEnum = MultiTerms.getTerms(ir, field).iterator();
    if (termsEnum != null) {
      AutomatonTermsEnum automatonTermsEnum =
          new AutomatonTermsEnum(
              termsEnum, new CompiledAutomaton(PrefixQuery.toAutomaton(new BytesRef(prefix))));
      while (automatonTermsEnum.next() != null) {
        terms.add(new Term(field, new BytesRef(automatonTermsEnum.term().utf8ToString())));
      }
    }

    return terms.toArray(new Term[terms.size()]);
  }

  public void indexBatch(final Collection<IndexedItem> batch) {
    modifyIndex(
        new IndexBuilder() {
          @Override
          public long buildIndex(SearcherManager searcherManager, IndexWriter writer)
              throws Exception {
            long generation = -1;
            generation = Math.max(generation, removeDocuments(batch, writer));
            generation = Math.max(generation, addDocuments(batch, writer));
            return generation;
          }
        });
  }

  public Operator getDefaultOperator() throws QueryNodeException {
    Operator o;

    if (this.defaultOperator.equalsIgnoreCase("and")) {
      o = Operator.AND;
    } else if (this.defaultOperator.equalsIgnoreCase("OR")) {
      o = Operator.OR;
    } else {
      throw new QueryNodeException(
          new MessageImpl(QueryParserMessages.INVALID_SYNTAX, "Invalid Default Operator (AND/OR)"));
    }

    return o;
  }

  public void setDefaultOperator(String defaultOperator) {
    this.defaultOperator = defaultOperator;
  }

  public void deleteForInstitution(final long id) {
    modifyIndex(
        new IndexBuilder() {
          @Override
          public long buildIndex(SearcherManager searcherManager, IndexWriter writer)
              throws Exception {
            writer.deleteDocuments(new Term(FreeTextQuery.FIELD_INSTITUTION, Long.toString(id)));
            return -1;
          }
        });
  }

  @Override
  protected Map<String, Analyzer> getAnalyzerFieldMap(Analyzer autoComplete, Analyzer nonStemmed) {
    return ImmutableMap.of(
        FreeTextQuery.FIELD_BODY_NOSTEM,
        nonStemmed,
        FreeTextQuery.FIELD_NAME_AUTOCOMPLETE,
        autoComplete,
        FreeTextQuery.FIELD_NAME_VECTORED_NOSTEM,
        nonStemmed,
        FreeTextQuery.FIELD_BOOKMARK_TAGS,
        nonStemmed,
        FreeTextQuery.FIELD_ATTACHMENT_VECTORED_NOSTEM,
        nonStemmed);
  }

  @Override
  public void checkHealth() {
    search(
        new Searcher<T>() {

          @Override
          public T search(IndexSearcher searcher) throws IOException {
            MultiTerms.getTerms(searcher.getIndexReader(), FreeTextQuery.FIELD_ALL)
                .iterator()
                .term();
            return null;
          }
        });
  }

  private static final class CountingCollector extends SimpleCollector {

    private int count = 0;

    public int getCount() {
      return count;
    }

    @Override
    public void collect(int doc) {
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
    public ScoreMode scoreMode() {
      return ScoreMode.COMPLETE_NO_SCORES;
    }
  }

  private static final class BitSetCollector extends SimpleCollector {

    private int docBase;
    private final FixedBitSet bitSet;

    private BitSetCollector(int maxDoc) {
      this.bitSet = new FixedBitSet(maxDoc);
    }

    public FixedBitSet getBitSet() {
      return bitSet;
    }

    @Override
    public void collect(int doc) {
      bitSet.set(docBase + doc);
    }

    @Override
    protected void doSetNextReader(LeafReaderContext context) {
      this.docBase = context.docBase;
    }

    @Override
    public ScoreMode scoreMode() {
      return ScoreMode.COMPLETE_NO_SCORES;
    }
  }

  private static final class CompressedSetCollector extends SimpleCollector {

    private final LongSet set = new LongSet(new ConciseSet());
    private int docBase;

    @Override
    public void collect(int docId) {
      set.add(docBase + docId);
    }

    public LongSet getSet() {
      return set;
    }

    @Override
    protected void doSetNextReader(LeafReaderContext context) {
      this.docBase = context.docBase;
    }

    @Override
    public ScoreMode scoreMode() {
      return ScoreMode.COMPLETE_NO_SCORES;
    }
  }
}
