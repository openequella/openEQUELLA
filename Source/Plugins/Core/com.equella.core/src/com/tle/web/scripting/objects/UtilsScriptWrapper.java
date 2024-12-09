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

package com.tle.web.scripting.objects;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.dytech.edge.common.PropBagWrapper;
import com.dytech.edge.common.valuebean.SearchRequest;
import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.scripting.objects.UtilsScriptObject;
import com.tle.common.scripting.types.*;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.common.searching.Search;
import com.tle.common.searching.Search.SortType;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.search.LegacySearch;
import com.tle.freetext.FreetextIndex;
import com.tle.web.viewurl.ViewItemUrlFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

@Bind(UtilsScriptObject.class)
@Singleton
@SuppressWarnings("nls")
public class UtilsScriptWrapper extends AbstractScriptWrapper implements UtilsScriptObject {
  private static final long serialVersionUID = 1L;

  @Inject private ItemDefinitionService collectionService;
  @Inject private FreeTextService freetextService;
  @Inject private FreetextIndex freetextIndex;
  @Inject private ViewItemUrlFactory urlFactory;

  @Override
  public CollectionScriptType getCollectionFromUuid(String uuid) {
    ItemDefinition collection = collectionService.getByUuid(uuid);
    if (collection != null) {
      return scriptTypeFactory.createCollection(collection);
    }
    return null;
  }

  @Override
  public Date getDate(String date, String format) throws ParseException {
    return new SimpleDateFormat(format).parse(date);
  }

  @Override
  public boolean isEmpty(String text) {
    return Check.isEmpty(text);
  }

  @Override
  public SearchResultsScriptType search(String query, int start, int length) {
    DefaultSearch req = new DefaultSearch();
    req.setQuery(query);
    req.setSortType(SortType.NAME);
    return scriptTypeFactory.createSearchResults(freetextService.search(req, start, length));
  }

  @Override
  public SearchResultsScriptType searchAdvanced(
      String query,
      String where,
      boolean onlyLive,
      int orderType,
      boolean reverseOrder,
      int offset,
      int maxResults) {
    SortType[] sortTypes = SortType.values();
    if (orderType >= sortTypes.length || orderType < 0) {
      throw new RuntimeException("Unrecognised sort order type");
    }
    SearchRequest req = new SearchRequest();
    req.setQuery(query);
    req.setOrderType(orderType);
    req.setSortReverse(reverseOrder);
    req.setWhere(where);
    req.setOnlyLive(onlyLive);

    return scriptTypeFactory.createSearchResults(
        freetextService.search(new LegacySearch(req, collectionService), offset, maxResults));
  }

  @Override
  public int queryCount(String[] collectionUuids, String where) throws Exception {
    Search search = createSearch(collectionUuids, where);
    return freetextService.countsFromFilters(Collections.singleton(search))[0];
  }

  @Override
  public List<FacetCountResultScriptType> facetCount(
      String query, String[] collectionUuids, String where, String facetXpath) {
    DefaultSearch search = createSearch(collectionUuids, where);
    search.setQuery(query);

    List<FacetCountResultScriptType> rv = Lists.newArrayList();

    if (facetXpath.startsWith("/xml")) {
      facetXpath = facetXpath.substring(facetXpath.indexOf('/', 1));
    }

    Multimap<String, Pair<String, Integer>> vcs =
        freetextIndex.facetCount(search, Collections.singletonList(facetXpath));
    for (Pair<String, Integer> valueCount : vcs.get(facetXpath)) {
      final String value = valueCount.getFirst();
      if (!Check.isEmpty(value)) {
        final int count = valueCount.getSecond();
        rv.add(
            new FacetCountResultScriptType() {
              @Override
              public String getTerm() {
                return value;
              }

              @Override
              public int getCount() {
                return count;
              }
            });
      }
    }

    Collections.sort(
        rv,
        new Comparator<FacetCountResultScriptType>() {
          @Override
          public int compare(FacetCountResultScriptType o1, FacetCountResultScriptType o2) {
            return o2.getCount() - o1.getCount();
          }
        });

    return rv;
  }

  private DefaultSearch createSearch(String[] collectionUuids, String where) {
    FreeTextQuery q = WhereParser.parse(where);

    Collection<String> collections = null;
    if (collectionUuids != null && collectionUuids.length > 0) {
      collections = Arrays.asList(collectionUuids);
    }

    DefaultSearch search = new DefaultSearch();
    search.setFreeTextQuery(q);
    search.setCollectionUuids(collections);
    return search;
  }

  @Override
  public ConnectionScriptType getConnection(String url) {
    try {
      return new ConnectionScriptTypeImpl(new URL(url));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void throwError(String message) {
    throw new RuntimeException(message);
  }

  @Override
  public String getItemUrl(ItemScriptType item) {
    return urlFactory.createFullItemUrl(new ItemId(item.getUuid(), item.getVersion())).getHref();
  }

  @Override
  public XmlScriptType newXmlDocument() {
    return new PropBagWrapper(new PropBagEx());
  }

  @Override
  public XmlScriptType newXmlDocumentFromString(String xmlString) {
    return new PropBagWrapper(new PropBagEx(xmlString));
  }

  public static class ConnectionScriptTypeImpl implements ConnectionScriptType {
    private static final long serialVersionUID = 1L;

    private final Multimap<String, String> formData = ArrayListMultimap.create();
    private final URL url;

    protected ConnectionScriptTypeImpl(URL url) {
      this.url = url;
    }

    @Override
    public void addFormData(String key, String value) {
      formData.get(key).add(value);
    }

    @Override
    public ResponseScriptType getResponse(boolean isGET) {
      try {
        final String data = getFormData();

        String extra = "";
        if (isGET && !Check.isEmpty(data)) {
          String qs = url.getQuery();
          if (Check.isEmpty(qs)) {
            extra += "?" + data;
          } else {
            extra += "?" + qs + "&" + data;
          }
        }
        final String urlString = url.toString() + extra;

        final URL finalUrl = new URL(urlString);
        final HttpURLConnection conn = (HttpURLConnection) finalUrl.openConnection();

        conn.setRequestMethod(isGET ? "GET" : "POST");
        conn.setAllowUserInteraction(false);
        conn.setInstanceFollowRedirects(true);

        conn.setDoOutput(true);
        conn.setDoInput(true);

        // Send data
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "*/*");

        if (!isGET) {
          try (Writer wr = new OutputStreamWriter(conn.getOutputStream())) {
            if (!isGET) {
              wr.write(data);
            }
            wr.flush();
          }
        }

        // Get the response
        final String contentType = conn.getContentType();

        try (InputStream is = conn.getInputStream()) {
          if (contentType == null || contentType.startsWith("text/")) {
            Reader rd = new InputStreamReader(is);
            StringWriter sw = new StringWriter();
            CharStreams.copy(rd, sw);
            return new ResponseScriptTypeImpl(sw.toString(), conn.getResponseCode(), contentType);
          } else {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ByteStreams.copy(is, os);
            return new ResponseScriptTypeImpl(
                os.toByteArray(), conn.getResponseCode(), contentType);
          }
        }
      } catch (IOException e) {
        return new ResponseScriptTypeImpl("ERROR: " + e.getMessage(), 400, "text/plain");
      }
    }

    private String getFormData() throws UnsupportedEncodingException {
      final StringBuilder data = new StringBuilder();
      boolean first = true;
      for (Entry<String, Collection<String>> entry : formData.asMap().entrySet()) {
        for (String value : entry.getValue()) {
          if (!first) {
            data.append('&');
          }
          data.append(URLEncoder.encode(entry.getKey(), Constants.UTF8))
              .append('=')
              .append(URLEncoder.encode(value, Constants.UTF8));
          first = false;
        }
      }
      return data.toString();
    }
  }

  public static class ResponseScriptTypeImpl implements ResponseScriptType {
    private final int code;
    private final String mimeType;
    private String textResponse;
    private byte[] byteResponse;

    public ResponseScriptTypeImpl(
        final String textResponse, final int code, final String mimeType) {
      this.textResponse = textResponse;
      this.code = code;
      this.mimeType = mimeType;
    }

    public ResponseScriptTypeImpl(
        final byte[] byteResponse, final int code, final String mimeType) {
      this.byteResponse = byteResponse;
      this.code = code;
      this.mimeType = mimeType;
    }

    @Override
    public String getAsText() {
      if (textResponse == null) {
        throw new RuntimeException("Response received from external URL was not text");
      }
      return textResponse;
    }

    @Override
    public XmlScriptType getAsXml() {
      return new PropBagWrapper(new PropBagEx(getAsText()));
    }

    @Override
    public XmlScriptType getHtmlAsXml() {
      try {
        XMLReader htmlParser = new Parser();
        htmlParser.setFeature(Parser.namespacesFeature, false);
        htmlParser.setFeature(Parser.namespacePrefixesFeature, false);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        DOMResult result = new DOMResult();
        transformer.transform(
            new SAXSource(htmlParser, new InputSource(new StringReader(getAsText()))), result);

        Node node = result.getNode();
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
          node = node.getFirstChild();
        }
        return new PropBagWrapper(new PropBagEx(node));
      } catch (Exception ex) {
        throw new RuntimeException(
            "Response received from external URL could not be tidied into XML", ex);
      }
    }

    @Override
    public BinaryDataScriptType getAsBinaryData() {
      if (byteResponse == null) {
        throw new RuntimeException("Response received from external URL was not binary");
      }
      return new BinaryDataScriptTypeImpl(byteResponse);
    }

    @Override
    public int getCode() {
      return code;
    }

    @Override
    public String getContentType() {
      return mimeType;
    }

    @Override
    public boolean isError() {
      return !((code >= HttpURLConnection.HTTP_OK && code < HttpURLConnection.HTTP_MULT_CHOICE));
    }
  }

  public static class BinaryDataScriptTypeImpl implements BinaryDataScriptType {
    private final byte[] data;

    public BinaryDataScriptTypeImpl(final byte[] data) {
      this.data = data;
    }

    @Override
    public long getLength() {
      return data.length;
    }

    /** Internal use ONLY. Do NOT use in scripts. */
    public byte[] getData() {
      return data;
    }
  }
}
