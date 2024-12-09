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

package com.tle.web.service.oai.legacy;

import ORG.oclc.oai.server.catalog.AbstractCatalog;
import ORG.oclc.oai.server.verb.BadArgumentException;
import ORG.oclc.oai.server.verb.BadResumptionTokenException;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import ORG.oclc.oai.server.verb.IdDoesNotExistException;
import ORG.oclc.oai.server.verb.NoItemsMatchException;
import ORG.oclc.oai.server.verb.NoMetadataFormatsException;
import ORG.oclc.oai.server.verb.NoRecordsMatchException;
import ORG.oclc.oai.server.verb.NoSetHierarchyException;
import ORG.oclc.oai.server.verb.OAIInternalServerError;
import ORG.oclc.oai.util.OAIUtil;
import com.dytech.edge.common.valuebean.ItemKey;
import com.google.common.base.Optional;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Search.SortType;
import com.tle.common.settings.standard.OAISettings;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.item.service.ItemService;
import com.tle.core.replicatedcache.ReplicatedCacheService;
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.settings.service.ConfigurationService;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
@SuppressWarnings("nls")
public class OAICatalog extends AbstractCatalog {
  private static final Logger LOGGER = LoggerFactory.getLogger(OAICatalog.class);
  private static final int MAX_RESULTS = 10;

  @Inject private FreeTextService freeTextService;
  @Inject private ItemService itemService;
  @Inject private ItemDefinitionService itemdefService;
  @Inject private SchemaService schemaService;
  @Inject private ConfigurationService configService;

  private ReplicatedCache<ResumptionToken> resumptionTokens;

  public OAICatalog(Properties properties // NOSONAR
      ) {
    // Nothing to do, but this constructor needs to exist, taking a
    // Properties object that we don't use. It is invoked by the OAI
    // framework using reflection.
  }

  @Inject
  public void setReplicatedCacheService(ReplicatedCacheService service) {
    resumptionTokens = service.getCache("oailegacy-resumptiontokens", 2000, 1, TimeUnit.HOURS);
  }

  @Override
  public Map<String, Iterator<String>> listSets()
      throws NoSetHierarchyException, OAIInternalServerError {
    Map<String, Iterator<String>> listSetsMap = new HashMap<String, Iterator<String>>();
    ArrayList<String> sets = new ArrayList<String>();

    for (ItemDefinition itemdef : itemdefService.enumerateSearchable()) {
      String name = CurrentLocale.get(itemdef.getName(), ""); // $NON-NLS-1$
      String description = CurrentLocale.get(itemdef.getDescription(), null);

      // Dodgy but no way to do this
      StringBuilder sb = new StringBuilder();
      sb.append("<set>"); // $NON-NLS-1$
      sb.append("<setSpec>"); // $NON-NLS-1$
      sb.append(OAIUtil.xmlEncode(itemdef.getUuid()));
      sb.append("</setSpec>"); // $NON-NLS-1$
      sb.append("<setName>"); // $NON-NLS-1$
      sb.append(OAIUtil.xmlEncode(name));
      sb.append("</setName>"); // $NON-NLS-1$
      if (description != null && description.length() > 0) {
        sb.append("<setDescription><oai_dc:dc ")
            .append("xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" ")
            .append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\" ")
            .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
            .append("xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ ")
            .append("http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">");
        sb.append("<dc:description>");
        sb.append(OAIUtil.xmlEncode(description));
        sb.append("</dc:description></oai_dc:dc></setDescription>");
      }
      sb.append("</set>"); // $NON-NLS-1$

      sets.add(sb.toString());
    }

    listSetsMap.put("sets", sets.iterator()); // $NON-NLS-1$
    return listSetsMap;
  }

  @Override
  public Map<?, ?> listSets(String resumption)
      throws ORG.oclc.oai.server.verb.BadResumptionTokenException, OAIInternalServerError {
    throw new BadResumptionTokenException();
  }

  // Return type of Vector determined by external jar
  @Override
  public Vector<?> getSchemaLocations(String identifier)
      throws IdDoesNotExistException,
          NoMetadataFormatsException, // NOSONAR
          OAIInternalServerError {
    Object nativeItem = getRecord(identifier);
    if (nativeItem == null) {
      throw new IdDoesNotExistException(identifier);
    } else {
      return getRecordFactory().getSchemaLocations(nativeItem);
    }
  }

  @Override
  public Map<?, ?> listIdentifiers(String from, String until, String set, String metadataPrefix)
      throws BadArgumentException,
          CannotDisseminateFormatException,
          NoItemsMatchException,
          NoSetHierarchyException,
          OAIInternalServerError {
    return list(set, from, until, metadataPrefix, new IdentifierHandler());
  }

  @Override
  public Map<?, ?> listIdentifiers(String resumption)
      throws BadResumptionTokenException, OAIInternalServerError {
    return list(resumption, new IdentifierHandler());
  }

  @Override
  public Map<?, ?> listRecords(String from, String until, String set, String metadataPrefix)
      throws BadArgumentException,
          CannotDisseminateFormatException,
          NoItemsMatchException,
          NoSetHierarchyException,
          OAIInternalServerError {
    return list(set, from, until, metadataPrefix, new RecordHandler());
  }

  @Override
  public Map<?, ?> listRecords(String resumptionToken)
      throws BadResumptionTokenException, OAIInternalServerError {
    return list(resumptionToken, new RecordHandler());
  }

  @Override
  public String getRecord(String identifier, String metadataPrefix)
      throws ORG.oclc.oai.server.verb.IdDoesNotExistException,
          ORG.oclc.oai.server.verb.CannotDisseminateFormatException,
          OAIInternalServerError {
    return constructRecord(getRecord(identifier), metadataPrefix);
  }

  private String constructRecord(Object nativeItem, String metadataPrefix)
      throws CannotDisseminateFormatException {
    String schemaURL = null;

    if (metadataPrefix != null) {
      schemaURL = getCrosswalks().getSchemaURL(metadataPrefix);
      if (schemaURL == null) {
        throw new CannotDisseminateFormatException(metadataPrefix);
      }
    }
    return getRecordFactory().create(nativeItem, schemaURL, metadataPrefix);
  }

  private Item getRecord(String identifier) throws IdDoesNotExistException {
    if (identifier.startsWith("tle:")) // $NON-NLS-1$
    {
      identifier = identifier.substring(4);
    }

    ItemId id;
    if (identifier.indexOf(':') > 0) {
      ItemKey key = new ItemKey(identifier);
      id = new ItemId(key.getUuid() + '/' + key.getVersion());
    } else if (identifier.indexOf('/') > 0) {
      id = new ItemId(identifier);
    } else {
      throw new IdDoesNotExistException(identifier);
    }

    Item nativeItem;
    try {
      nativeItem = itemService.get(id);
    } catch (Exception e) {
      throw new IdDoesNotExistException(e.getMessage());
    }
    return nativeItem;
  }

  @Override
  public void close() {
    // NOTHING TO DO
  }

  private static class ResumptionToken implements Serializable {
    final DefaultSearch request;
    final String format;
    final int start;

    public ResumptionToken(int start, DefaultSearch request, String format) {
      this.start = start;
      this.request = request;
      this.format = format;
    }
  }

  private Map<?, ?> list(String stoken, Handler handler)
      throws BadResumptionTokenException, OAIInternalServerError {
    Optional<ResumptionToken> maybeToken = resumptionTokens.get(stoken);
    if (!maybeToken.isPresent()) {
      throw new BadResumptionTokenException();
    }

    try {
      // Token is only valid for one use
      resumptionTokens.invalidate(stoken);

      ResumptionToken token = maybeToken.get();
      return list(token.request, token.format, token.start + MAX_RESULTS, handler);
    } catch (NoRecordsMatchException e) {
      LOGGER.error("No records match", e); // $NON-NLS-1$
      throw new BadResumptionTokenException();
    }
  }

  private Map<?, ?> list(
      String set, String from, String until, String metadataFormat, Handler handler)
      throws NoRecordsMatchException, BadArgumentException, OAIInternalServerError {
    DefaultSearch request = new DefaultSearch();
    request.setSortType(SortType.DATEMODIFIED);
    Collection<ItemDefinition> list = null;

    OAISettings props = configService.getProperties(new OAISettings());

    if (props.isUseDownloadItemAcl()) {
      request.setPrivilege("DOWNLOAD_ITEM");
    }

    try {
      if (set != null) {
        list = itemdefService.getMatchingSearchableUuid(Collections.singleton(set));
      } else {
        list = itemdefService.enumerateSearchable();
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      throw new OAIInternalServerError(e.getMessage());
    }

    request.setCollectionUuids(itemdefService.convertToUuids(list));

    if (!metadataFormat.equalsIgnoreCase(TLECrosswalk.DC_FORMAT)) {
      request.setSchemas(schemaService.getSchemasForExportSchemaType(metadataFormat));
    }

    if (from != null || until != null) {
      Date[] range = new Date[2];
      try {
        if (from != null) {
          range[0] = new UtcDate(from, Dates.ISO).toDate();
        }
      } catch (ParseException ex) {
        throw new BadArgumentException();
      }

      try {
        if (until != null) {
          range[1] = new UtcDate(until, Dates.ISO).toDate();
        }
      } catch (ParseException ex) {
        throw new BadArgumentException();
      }
      request.setDateRange(range);
    }
    return list(request, metadataFormat, 0, handler);
  }

  private Map<String, Map<?, ?>> list(
      DefaultSearch request, String format, int start, Handler handler)
      throws NoRecordsMatchException, OAIInternalServerError {
    try {
      Map<String, Map<?, ?>> items = new HashMap<String, Map<?, ?>>();

      FreetextSearchResults<FreetextResult> results =
          freeTextService.search(request, start, MAX_RESULTS);

      int available = results.getAvailable();
      if (available == 0) {
        throw new NoRecordsMatchException();
      }
      for (Item item : results.getResults()) {
        handler.add(item, format);
      }

      if (available > start + MAX_RESULTS) {
        String uuid = UUID.randomUUID().toString();
        Map<?, ?> map = getResumptionMap(uuid, available, start);
        ResumptionToken token = new ResumptionToken(start, request, format);
        resumptionTokens.put(uuid, token);
        items.put("resumptionMap", map); // $NON-NLS-1$
      }
      handler.put(items);
      return items;
    } catch (NoRecordsMatchException e) // NOSONAR - rethrow, no conversion
    {
      throw e;
    } catch (Exception e) {
      LOGGER.error("Error searching freetext", e); // $NON-NLS-1$
      throw new OAIInternalServerError(e.getMessage());
    }
  }

  private interface Handler {
    String getSelect();

    void add(Item item, String metadataPrefix) throws Exception;

    void put(Map<?, ?> map);
  }

  private class RecordHandler implements Handler {
    private final List<String> records;

    public RecordHandler() {
      records = new ArrayList<String>();
    }

    @Override
    public String getSelect() {
      return "*"; //$NON-NLS-1$
    }

    @Override
    public void add(Item item, String metadataPrefix)
        throws IllegalArgumentException, CannotDisseminateFormatException {
      String schemaURL = null;
      if (metadataPrefix != null) {
        schemaURL = getCrosswalks().getSchemaURL(metadataPrefix);
        if (schemaURL == null) {
          throw new CannotDisseminateFormatException(metadataPrefix);
        }
      }
      records.add(getRecordFactory().create(item, schemaURL, metadataPrefix));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void put(Map map) {
      map.put("records", records.iterator()); // $NON-NLS-1$
    }
  }

  private class IdentifierHandler implements Handler {
    private final List<String> identifiers;
    private final List<String> headers;

    public IdentifierHandler() {
      identifiers = new ArrayList<String>();
      headers = new ArrayList<String>();
    }

    @Override
    public String getSelect() {
      return "/xml/item/@id, /xml/item/@itemdefid, /xml/item/@version," //$NON-NLS-1$
          + " /xml/item/datemodified"; //$NON-NLS-1$
    }

    @Override
    public void add(Item item, String metadataPrefix) throws Exception {
      String[] header = getRecordFactory().createHeader(item);
      headers.add(header[0]);
      identifiers.add(header[1]);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void put(Map map) {
      map.put("identifiers", identifiers.iterator()); // $NON-NLS-1$
      map.put("headers", headers.iterator()); // $NON-NLS-1$
    }
  }
}
