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

package com.tle.core.reporting;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.ValueThoroughIterator;
import com.dytech.edge.exceptions.SearchingException;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.Check;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.common.searching.Search;
import com.tle.common.searching.SearchResults;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemService;
import com.tle.core.remoting.MatrixResults;
import com.tle.core.remoting.MatrixResults.MatrixEntry;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.item.FreetextResult;
import com.tle.freetext.FreetextIndex;
import com.tle.reporting.IResultSetExt;
import com.tle.reporting.MetadataBean;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.log4j.Logger;

@Bind
@Singleton
public class FreetextQueryDelegate extends SimpleTypeQuery {
  private static final Logger LOGGER = Logger.getLogger(FreetextQueryDelegate.class);

  @Inject private FreetextIndex freetextIndex;
  @Inject private FileSystemService fsysService;
  @Inject private ItemService itemService;
  @Inject private ItemFileService itemFileService;

  enum FTQueryType {
    QUERY("q"),
    COUNT("count"),
    MATRIX("m"),
    MATRIX_COUNT("mc"),
    FILES("f"); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    private final String prefix;
    private static Map<String, FTQueryType> prefixMap;

    FTQueryType(String prefix) {
      this.prefix = prefix;
      addToPrefix(this);
    }

    private void addToPrefix(FTQueryType type) {
      if (prefixMap == null) {
        prefixMap = new HashMap<String, FTQueryType>();
      }
      prefixMap.put(prefix, this);
    }

    public String getPrefix() {
      return prefix;
    }

    public static FTQueryType getType(String prefix) {
      return prefixMap.get(prefix);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.tle.core.reporting.QueryDelegate#getDatasourceMetadata()
   */
  @Override
  public Map<String, ?> getDatasourceMetadata() {
    throw new RuntimeException("Functionality not complete"); // $NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * @see com.tle.core.reporting.QueryDelegate#executeQuery(java.lang.String,
   * java.util.List)
   */
  @Override
  public IResultSetExt executeQuery(String query, List<Object> params, int maxRows) {
    FTQueryType queryType = getQueryType(query);
    String[] queryStrings = getQueryStrings(query, params);
    Search search;
    if (maxRows <= 0) {
      maxRows = Integer.MAX_VALUE;
    }
    switch (queryType) {
      case QUERY:
        return processQuery(queryStrings, maxRows);

      case MATRIX_COUNT:
        return processMatrixCount(queryStrings, maxRows);

      case MATRIX:
        return processMatrix(queryStrings, maxRows);

      case COUNT:
        MetadataBean bean = new MetadataBean();
        search = setupSearchRequest(queryStrings, 0);
        int count = freetextIndex.count(search);
        addColumn("count", TYPE_INT, bean); // $NON-NLS-1$
        return new SimpleResultSet(new Object[][] {new Object[] {count}}, bean);

      case FILES:
        return processFiles(maxRows, queryStrings);
    }
    throw new UnsupportedOperationException("Unknown type:" + queryType); // $NON-NLS-1$
  }

  private IResultSetExt processFiles(int maxRows, String[] queryStrings) {
    MetadataBean bean = new MetadataBean();
    Search search = setupSearchRequest(queryStrings, 0);
    SearchResults<FreetextResult> results = freetextIndex.search(search, 0, maxRows);
    List<Object[]> resultsList = new ArrayList<Object[]>();
    List<FreetextResult> idResults = results.getResults();
    for (FreetextResult fresult : idResults) {
      ItemIdKey result = fresult.getItemIdKey();
      ItemFile handle = itemFileService.getItemFile(ItemId.fromKey(result), null);
      try {
        FileEntry entry = fsysService.enumerateTree(handle, "", null); // $NON-NLS-1$
        recurseEntries(result.getKey(), entry, resultsList, ""); // $NON-NLS-1$
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    addColumn("key", TYPE_LONG, bean); // $NON-NLS-1$
    addColumn("name", TYPE_STRING, bean); // $NON-NLS-1$
    addColumn("path", TYPE_STRING, bean); // $NON-NLS-1$
    addColumn("filesize", TYPE_INT, bean); // $NON-NLS-1$
    return new SimpleResultSet(resultsList, bean);
  }

  private IResultSetExt processMatrix(String[] queryStrings, int maxRows) {
    MetadataBean bean = new MetadataBean();
    Search search = setupSearchRequest(queryStrings, 1);
    List<String> fields = getFields(queryStrings[0]);
    ArrayList<Object[]> resultSet = new ArrayList<Object[]>();
    MatrixResults results = freetextIndex.matrixSearch(search, fields, false);
    List<MatrixEntry> entries = results.getEntries();
    for (MatrixEntry entry : entries) {
      List<ItemIdKey> items = entry.getItems();
      for (ItemIdKey key : items) {
        List<String> fieldValues = entry.getFieldValues();
        Object[] row = new Object[fieldValues.size() + 3];
        int i = 0;
        for (String val : fieldValues) {
          row[i++] = val;
        }
        row[i++] = new BigDecimal(key.getKey());
        row[i++] = key.getUuid();
        row[i++] = key.getVersion();
        resultSet.add(row);
        if (resultSet.size() >= maxRows) {
          break;
        }
      }
    }
    for (String field : fields) {
      addColumn(field, TYPE_STRING, bean);
    }
    addColumn("key", TYPE_LONG, bean); // $NON-NLS-1$
    addColumn("uuid", TYPE_STRING, bean); // $NON-NLS-1$
    addColumn("version", TYPE_INT, bean); // $NON-NLS-1$
    return new SimpleResultSet(resultSet, bean);
  }

  private IResultSetExt processMatrixCount(String[] queryStrings, int maxRows) {
    MetadataBean bean = new MetadataBean();
    Search search = setupSearchRequest(queryStrings, 1);
    List<String> fields = getFields(queryStrings[0]);
    List<Object[]> resultSet = new ArrayList<Object[]>();
    MatrixResults results = freetextIndex.matrixSearch(search, fields, true);
    List<MatrixEntry> entries = results.getEntries();
    for (MatrixEntry entry : entries) {
      List<String> fieldValues = entry.getFieldValues();
      Object[] row = new Object[fieldValues.size() + 1];
      int i = 0;
      for (String val : fieldValues) {
        row[i++] = val;
      }
      row[i++] = entry.getCount();
      resultSet.add(row);
      if (resultSet.size() >= maxRows) {
        break;
      }
    }
    for (String field : fields) {
      addColumn(field, TYPE_STRING, bean);
    }
    addColumn("count", TYPE_INT, bean); // $NON-NLS-1$
    return new SimpleResultSet(resultSet, bean);
  }

  private IResultSetExt processQuery(String[] queryStrings, int maxRows) {
    Search search = setupSearchRequest(queryStrings, 0);
    List<String> fields = Collections.emptyList();
    if (queryStrings.length > 2) {
      fields = getFields(queryStrings[2]);
    }

    List<Object[]> resultSet = new ArrayList<Object[]>();
    try {
      final SearchResults<FreetextResult> results = freetextIndex.search(search, 0, maxRows);

      for (FreetextResult fresult : results.getResults()) {
        ItemIdKey result = fresult.getItemIdKey();
        List<Object> row = new ArrayList<Object>();
        row.add(new BigDecimal(result.getKey()));
        row.add(result.getUuid());
        row.add(result.getVersion());

        if (fields.size() > 0) {
          PropBagEx xml = itemService.getItemXmlPropBag(result);
          for (String field : fields) {
            ValueThoroughIterator vals = xml.iterateAllValues(field);
            if (vals.hasNext()) {
              row.add(vals.next());
            } else {
              row.add(""); // $NON-NLS-1$
            }
          }
        }
        resultSet.add(row.toArray(new Object[row.size()]));

        if (resultSet.size() >= maxRows) {
          break;
        }
      }
    } catch (SearchingException se) {
      LOGGER.error("Error searching", se); // $NON-NLS-1$
      throw new RuntimeException("Error searching"); // $NON-NLS-1$
    }
    MetadataBean bean = new MetadataBean();
    addColumn("key", TYPE_LONG, bean); // $NON-NLS-1$
    addColumn("uuid", TYPE_STRING, bean); // $NON-NLS-1$
    addColumn("version", TYPE_INT, bean); // $NON-NLS-1$
    for (String field : fields) {
      addColumn(field, TYPE_STRING, bean);
    }
    return new SimpleResultSet(resultSet, bean);
  }

  private FTQueryType getQueryType(String query) {
    return FTQueryType.getType(query.substring(0, query.indexOf(':')));
  }

  private List<String> getFields(String query) {
    StringTokenizer stok = new StringTokenizer(query, ","); // $NON-NLS-1$
    List<String> fields = new ArrayList<String>();
    while (stok.hasMoreTokens()) {
      fields.add(stok.nextToken().trim());
    }
    return fields;
  }

  private Search setupSearchRequest(String[] queryStrings, int offset) {
    if (queryStrings.length <= offset) {
      return null;
    }
    DefaultSearch search = new DefaultSearch();
    if (queryStrings.length > (1 + offset)) {
      search.setFreeTextQuery(WhereParser.parse(queryStrings[1 + offset]));
    }
    search.setQuery(queryStrings[offset]);
    return search;
  }

  private void recurseEntries(long id, FileEntry entry, List<Object[]> results, String parent) {
    String name = entry.getName();
    if (name != null) {
      parent = parent + '/' + entry.getName();
      if (Check.isEmpty(entry.getFiles())) {
        results.add(
            new Object[] {new BigDecimal(id), entry.getName(), parent, (int) entry.getLength()});
      }
    }
    for (FileEntry file : entry.getFiles()) {
      recurseEntries(id, file, results, parent);
    }
  }
}
