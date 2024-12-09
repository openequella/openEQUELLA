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

package com.tle.web.service.oai;

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
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.tle.beans.entity.DynaCollection;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Search.SortType;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.service.ItemService;
import com.tle.core.replicatedcache.ReplicatedCacheService;
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.search.QueryGatherer;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.settings.service.ConfigurationService;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
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

/**
 * This uses Dynamic Collections rather than normal Collections. If a DC is virtualised, its value
 * is encoded into the OAI Set identifier as UUID:VirtualisedValue
 */
@SuppressWarnings("nls")
public class OAICatalog extends AbstractCatalog {
  private static final Logger LOGGER = LoggerFactory.getLogger(OAICatalog.class);
  private static final int MAX_RESULTS = 10;
  private static final String OAI_USAGE = "oaiUsage";

  @Inject private FreeTextService freeTextService;
  @Inject private ItemService itemService;
  @Inject private DynaCollectionService dynaCollectionService;
  @Inject private SchemaService schemaService;
  @Inject private ConfigurationService configService;
  @Inject private InstitutionService institutionService;

  private ReplicatedCache<ResumptionToken> resumptionTokens;

  public OAICatalog(Properties props // NOSONAR
      ) {
    // Nothing to do, but this constructor needs to exist, taking a
    // Properties object that we don't use. It is invoked by the OAI
    // framework using reflection.
  }

  @Inject
  public void setReplicatedCacheService(ReplicatedCacheService service) {
    resumptionTokens = service.getCache("oai-resumptiontokens", 2000, 1, TimeUnit.HOURS);
  }

  @Override
  public String getDescriptions() {
    OAIUtils utils = OAIUtils.getInstance(institutionService, configService);

    StringBuilder s = new StringBuilder();
    s.append("<description><oai-identifier");
    s.append("     xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai-identifier");
    s.append("     http://www.openarchives.org/OAI/2.0/oai-identifier.xsd\"");
    s.append("     xmlns=\"http://www.openarchives.org/OAI/2.0/oai-identifier\"");
    s.append("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
    s.append("<scheme>");
    s.append(utils.getScheme());
    s.append("</scheme><repositoryIdentifier>");
    s.append(utils.getNamespaceIdentifier());
    s.append("</repositoryIdentifier><delimiter>:</delimiter><sampleIdentifier>");
    s.append(utils.getSampleIdentifier());
    s.append("</sampleIdentifier></oai-identifier></description>");
    return s.toString();
  }

  @Override
  public Map<String, Iterator<String>> listSets()
      throws NoSetHierarchyException, OAIInternalServerError {
    return Collections.singletonMap(
        "sets",
        Lists.transform(
                dynaCollectionService.enumerateExpanded(OAI_USAGE),
                new Function<VirtualisableAndValue<DynaCollection>, String>() {
                  @Override
                  public String apply(VirtualisableAndValue<DynaCollection> pair) {
                    final DynaCollection dynaColl = pair.getVt();
                    final String virtualiseValue = pair.getVirtualisedValue();
                    final String name = CurrentLocale.get(dynaColl.getName(), "");
                    final String description = CurrentLocale.get(dynaColl.getDescription(), "");

                    StringBuilder sb = new StringBuilder();
                    sb.append("<set><setSpec>");
                    sb.append(OAIUtil.xmlEncode(dynaColl.getUuid()));
                    if (!Check.isEmpty(virtualiseValue)) {
                      sb.append(':');
                      // The setSpec element cannot contain spaces, so we
                      // need to URL encode the value to remove any.
                      sb.append(URLUtils.basicUrlEncode(virtualiseValue));
                    }
                    sb.append("</setSpec><setName>");
                    sb.append(OAIUtil.xmlEncode(name));
                    sb.append("</setName>");
                    if (!Check.isEmpty(description)) {
                      sb.append("<setDescription><oai_dc:dc ")
                          .append("xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" ")
                          .append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\" ")
                          .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
                          .append(
                              "xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ ")
                          .append("http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">");
                      sb.append("<dc:description>");
                      sb.append(OAIUtil.xmlEncode(description));
                      sb.append("</dc:description></oai_dc:dc></setDescription>");
                    }
                    sb.append("</set>"); // $NON-NLS-1$
                    return sb.toString();
                  }
                })
            .iterator());
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
  public String getRecord(String identifier, String mdPrefix)
      throws ORG.oclc.oai.server.verb.IdDoesNotExistException,
          ORG.oclc.oai.server.verb.CannotDisseminateFormatException,
          OAIInternalServerError {
    String schemaURL = mdPrefix != null ? getCrosswalks().getSchemaURL(mdPrefix) : null;
    if (schemaURL == null) {
      throw new CannotDisseminateFormatException(mdPrefix);
    }
    return getRecordFactory().create(getRecord(identifier), schemaURL, mdPrefix);
  }

  private Item getRecord(String identifier) throws IdDoesNotExistException {
    final OAIUtils utils = OAIUtils.getInstance(institutionService, configService);
    final ItemId id = utils.parseRecordIdentifier(identifier);
    try {
      return itemService.get(id);
    } catch (Exception e) {
      throw new IdDoesNotExistException(e.getMessage());
    }
  }

  @Override
  public void close() {
    // NOTHING TO DO
  }

  private static class ResumptionToken implements Serializable {
    private final DefaultSearch request;
    private final String format;
    private final int start;

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
      LOGGER.error("No records match", e);
      throw new BadResumptionTokenException();
    }
  }

  private Map<?, ?> list(
      String set, String from, String until, String metadataFormat, Handler handler)
      throws NoRecordsMatchException, BadArgumentException, OAIInternalServerError {
    final OAIUtils utils = OAIUtils.getInstance(institutionService, configService);
    DefaultSearch search = new DefaultSearch();
    search.setSortType(SortType.DATEMODIFIED);

    if (utils.isUseDownloadItemAcl()) {
      search.setPrivilege("DOWNLOAD_ITEM");
    }

    if (!Check.isEmpty(set)) {
      prepareSearchForSet(search, set);
    } else {
      prepareSearchForAllSets(search);
    }

    // Double-check that this is what we want to do
    search.setSchemas(schemaService.getSchemasForExportSchemaType(metadataFormat));

    if (from != null || until != null) {
      search.setDateRange(new Date[] {parseDate(from), parseDate(until)});
    }

    return list(search, metadataFormat, 0, handler);
  }

  private Date parseDate(String s) throws BadArgumentException {
    try {
      return s != null ? new UtcDate(s, Dates.ISO).toDate() : null;
    } catch (ParseException ex) {
      throw new BadArgumentException();
    }
  }

  private void prepareSearchForSet(DefaultSearch search, String set) {
    // Split for possible UUID:VirtualisationValue
    final String[] vs = set.split(":", 2);
    final DynaCollection dc = dynaCollectionService.getByUuid(vs[0]);
    dynaCollectionService.assertUsage(dc, OAI_USAGE);

    // The value (if available) is URL encoded, so undo that.
    prepareSearch(search, dc, vs.length > 1 ? URLUtils.basicUrlDecode(vs[1]) : null);
  }

  private void prepareSearchForAllSets(DefaultSearch search) {
    List<VirtualisableAndValue<DynaCollection>> dcs =
        dynaCollectionService.enumerateExpanded(OAI_USAGE);
    switch (dcs.size()) {
      case 0:
        return;

      case 1:
        VirtualisableAndValue<DynaCollection> pair = dcs.get(0);
        prepareSearch(search, pair.getVt(), pair.getVirtualisedValue());
        return;

      default:
        QueryGatherer q = new QueryGatherer(false);
        FreeTextBooleanQuery ftq = new FreeTextBooleanQuery(false, false);
        for (VirtualisableAndValue<DynaCollection> dcValue : dcs) {
          DynaCollection dc = dcValue.getVt();
          q.add(dynaCollectionService.getFreeTextQuery(dc));
          ftq.add(dynaCollectionService.getSearchClause(dc, dcValue.getVirtualisedValue()));
        }
        search.setQuery(q.toString());
        search.setFreeTextQuery(ftq);
    }
  }

  private void prepareSearch(DefaultSearch search, DynaCollection dc, String virtualiseValue) {
    search.setQuery(dynaCollectionService.getFreeTextQuery(dc));
    search.setFreeTextQuery(dynaCollectionService.getSearchClause(dc, virtualiseValue));
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
      LOGGER.error("Error searching freetext", e);
      throw new OAIInternalServerError(e.getMessage());
    }
  }

  private interface Handler {
    void add(Item item, String metadataPrefix) throws Exception;

    void put(Map<?, ?> map);
  }

  private class RecordHandler implements Handler {
    private final List<String> records;

    public RecordHandler() {
      records = new ArrayList<String>();
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
