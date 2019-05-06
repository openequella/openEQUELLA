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

package com.tle.core.remoterepo.srw.service.impl;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.SRWSettings;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.core.fedsearch.GenericRecord;
import com.tle.core.fedsearch.impl.BasicRecord;
import com.tle.core.fedsearch.impl.NullRecord;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.parser.mods.impl.loose.LooseModsRecord;
import com.tle.core.remoterepo.srw.service.SrwService;
import com.tle.core.xslt.service.XsltService;
import gov.loc.www.zing.srw.DiagnosticsType;
import gov.loc.www.zing.srw.RecordType;
import gov.loc.www.zing.srw.RecordsType;
import gov.loc.www.zing.srw.SearchRetrieveRequestType;
import gov.loc.www.zing.srw.SearchRetrieveResponseType;
import gov.loc.www.zing.srw.StringOrXmlFragment;
import gov.loc.www.zing.srw.diagnostic.DiagnosticType;
import gov.loc.www.zing.srw.interfaces.SRWPort;
import gov.loc.www.zing.srw.service.SRWSampleServiceLocator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.rpc.ServiceException;
import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.apache.axis.types.NonNegativeInteger;
import org.apache.axis.types.PositiveInteger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author agibb
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind(SrwService.class)
@Singleton
public class SrwServiceImpl implements SrwService {
  private XsltService xsltService;

  @Override
  public SrwSearchResults search(FederatedSearch srwSearch, String qs, int offset, int perpage) {
    SRWSettings settings = null;
    try {
      List<SrwSearchResult> results = new ArrayList<SrwSearchResult>();
      if (Check.isEmpty(qs)) {
        return new SrwSearchResults(results, 0, 0, 0);
      }

      // Setup Search
      settings = getSettings(srwSearch);
      SRWPort port = setupPort(settings);
      SearchRetrieveRequestType request = setupRequest(qs, offset, perpage, settings);

      // Do Search
      SearchRetrieveResponseType response = port.searchRetrieveOperation(request);
      RecordsType records = response.getRecords();
      int available = response.getNumberOfRecords().intValue();
      int count = (perpage > available ? available : perpage);

      // Show any problems with the search.
      String diagnosticMsg = doDiagnostic(response);

      // Build Results List
      if (records != null) {
        results = buildResults(records, offset);
      }

      SrwSearchResults srwSearchResults = new SrwSearchResults(results, count, offset, available);
      if (!Check.isEmpty(diagnosticMsg)) {
        srwSearchResults.setErrorMessage(diagnosticMsg);
      }

      return srwSearchResults;
    } catch (AxisFault af) // timeouts etc
    {
      SrwSearchResults srwSearchResultError = new SrwSearchResults(null, 0, 0, 0);
      String errorMessage = af.getFaultReason();
      String localizedMsg = af.getLocalizedMessage();
      if (!errorMessage.equalsIgnoreCase(localizedMsg)) {
        errorMessage += ": " + localizedMsg;
      }
      errorMessage +=
          " - " + (settings != null ? settings.getUrl() : " (undetermined or null URL )");
      srwSearchResultError.setErrorMessage(errorMessage);
      return srwSearchResultError;
    } catch (Exception e) // something unexpected
    {
      throw new RuntimeException(e);
    }
  }

  private SRWPort setupPort(SRWSettings settings) throws MalformedURLException, ServiceException {
    SRWSampleServiceLocator service = new SRWSampleServiceLocator();
    URL url = new URL(settings.getUrl());
    SRWPort port = service.getSRW(url);
    return port; // NOSONAR (keeping local var for readability)
  }

  private List<SrwSearchResult> buildResults(RecordsType records, int offset) throws Exception {
    List<SrwSearchResult> results = new ArrayList<SrwSearchResult>();
    int index = offset;

    RecordType[] record = records.getRecord();
    if (record != null) {
      for (RecordType r : record) {
        if (r != null) {
          results.add(convertRecordToSearchResult(r, index));
        }
        index++;
      }
    }
    return results;
  }

  private SrwSearchResult convertRecordToSearchResult(RecordType r, int index) throws Exception {
    StringOrXmlFragment frag = r.getRecordData();
    MessageElement[] elems = frag.get_any();

    SrwSearchResult result = new SrwSearchResult(index);
    // FIXME: English string
    String title = "Unknown";
    String description = "";
    String surl = null;
    if (elems.length > 0) {
      Element asDOM = elems[0].getAsDOM();
      PropBagEx xml = new PropBagEx(asDOM);
      String recordSchema = r.getRecordSchema();
      String rootElemName = xml.getRootElement().getNodeName();

      // a response error here not expected ...?
      if (rootElemName.endsWith("diagnostic")) {
        String diagnosticMsg = doDiagnostic(xml.getRootElement());
        throw new RuntimeException(diagnosticMsg);
      }

      if (matchesEither(MARCXML, recordSchema, asDOM.getNamespaceURI())) {
        LooseModsRecord mods = new LooseModsRecord(transformMarcToMods(xml));
        title = mods.getTitle();
        description = mods.getDescription();
        surl = mods.getUrl();
      } else if (matchesEither(DC, recordSchema, asDOM.getNamespaceURI())) {
        title = xml.getNode("title");
        description = xml.getNode("description");
        surl = xml.getNode("identifier");
        if (surl.length() == 0) {
          surl = null;
        }
      } else if (matchesEither(LOM, recordSchema, asDOM.getNamespaceURI())) {
        title = xml.getNode("general/title/string");
        description = xml.getNode("general/description/string");
        if (Check.isEmpty(title) && Check.isEmpty(description)) {
          // if the string element ending the xpath isn't productive
          // try langstring.
          title = xml.getNode("general/title/langstring");
          description = xml.getNode("general/description/langstring");
        }
        surl = xml.getNode("technical/location");
        if (surl.length() == 0) {
          surl = null;
        }
      } else if (matchesEither(MODS, recordSchema, asDOM.getNamespaceURI())) {
        LooseModsRecord mods = new LooseModsRecord(xml);
        title = mods.getTitle();
        description = mods.getDescription();
        surl = mods.getUrl();
      } else if (matchesEither(TLE, recordSchema, asDOM.getNamespaceURI())) {
        title = xml.getNode("item/name");
        description = xml.getNode("item/description");
        surl =
            xml.getNode("institutionUrl")
                + "items/"
                + xml.getNode("item/@uuid")
                + "/"
                + xml.getNode("item/@version");
      }

      result.setTitle(title);
      result.setDescription(description == null ? "" : description);
      result.setUrl(surl);
    }
    return result;
  }

  private PropBagEx transformMarcToMods(PropBagEx xml) {
    try {
      String xslt =
          Resources.toString(getClass().getResource("MARC21slim2MODS3-3.xsl"), Charsets.UTF_8);
      String transXml = xsltService.transformFromXsltString(xslt, xml);
      return new PropBagEx(transXml);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private GenericRecord convertRecordToGenericRecord(RecordType r) throws Exception {
    StringOrXmlFragment frag = r.getRecordData();
    MessageElement[] elems = frag.get_any();

    if (elems.length > 0) {
      Element asDOM = elems[0].getAsDOM();
      PropBagEx xml = new PropBagEx(asDOM);
      String recordSchema = r.getRecordSchema();

      if (matchesEither(MARCXML, recordSchema, asDOM.getNamespaceURI())) {
        return new LooseModsRecord(transformMarcToMods(xml));
      } else if (matchesEither(DC, recordSchema, asDOM.getNamespaceURI())) {
        // TODO: a DC parser
        BasicRecord basic = new BasicRecord();
        basic.setXml(xml);
        basic.setTitle(xml.getNode("title"));
        basic.setDescription(xml.getNode("description"));
        basic.setUrl(xml.getNode("identifier"));
        return basic;
      } else if (matchesEither(LOM, recordSchema, asDOM.getNamespaceURI())) {
        BasicRecord basic = new BasicRecord();
        basic.setXml(xml);
        String title = xml.getNode("general/title/string");
        String description = xml.getNode("general/description/string");
        if (Check.isEmpty(title) && Check.isEmpty(description)) {
          // if the string element ending the xpath isn't productive
          // try langstring.
          title = xml.getNode("general/title/langstring");
          description = xml.getNode("general/description/langstring");
        }
        basic.setTitle(title);
        basic.setDescription(description);
        basic.setUrl(xml.getNode("technical/location"));
        return basic;
      } else if (matchesEither(MODS, recordSchema, asDOM.getNamespaceURI())) {
        return new LooseModsRecord(xml);
      } else if (matchesEither(TLE, recordSchema, asDOM.getNamespaceURI())) {
        BasicRecord basic = new BasicRecord();
        basic.setXml(xml);
        basic.setTitle(xml.getNode("item/name"));
        basic.setDescription(xml.getNode("item/description"));
        basic.setUrl(
            xml.getNode("institutionUrl")
                + "items/"
                + xml.getNode("item/@uuid")
                + "/"
                + xml.getNode("item/@version"));
        return basic;
      }
    }
    return new NullRecord();
  }

  private String doDiagnostic(SearchRetrieveResponseType response) {
    String diagnosticMsg = null;
    DiagnosticsType diagnostics = response.getDiagnostics();
    if (diagnostics != null) {
      DiagnosticType[] diagnostic = diagnostics.getDiagnostic();
      if (diagnostic != null) {
        for (DiagnosticType d : diagnostic) {
          String msg = d.getMessage();
          if (msg == null) {
            diagnosticMsg = d.getUri().toString();
          }
        }
      }
    }
    return diagnosticMsg;
  }

  private String doDiagnostic(Node rootNode) {
    NodeList nodeList = rootNode.getChildNodes();
    String uri = null, details = null, message = null, wholeMsg = null;
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Node node = nodeList.item(i);
      if (node.getNodeName().equals("uri")) {
        uri = node.getTextContent();
      } else if (node.getNodeName().equals("details")) {
        details = node.getTextContent();
      } else if (node.getNodeName().equals("message")) {
        message = node.getTextContent();
      }
    }

    if (!Check.isEmpty(details)) {
      wholeMsg = "details: " + details;
    }

    if (!Check.isEmpty(message)) {
      if (wholeMsg == null) {
        wholeMsg = "";
      } else if (wholeMsg.length() > 0) {
        wholeMsg += ", ";
      }
      wholeMsg = wholeMsg + "message: " + message;
    }

    if (uri != null && uri.length() > 0) {
      if (wholeMsg == null) {
        wholeMsg = "";
      } else if (wholeMsg.length() > 0) {
        wholeMsg += ", ";
      }
      wholeMsg = wholeMsg + "uri: " + uri;
    }

    if (Check.isEmpty(wholeMsg)) {
      wholeMsg = "details unspecified";
    }

    return wholeMsg;
  }

  private SearchRetrieveRequestType setupRequest(
      String qs, int offset, int perpage, SRWSettings settings) {
    SearchRetrieveRequestType request = new SearchRetrieveRequestType();
    request.setVersion(SRW_VERSION);

    // Query strings need to be wrapped in double-quotes if they contain
    // special characters or white-space. It's safe to just always wrap it:
    // http://www.loc.gov/standards/sru/specs/cql.html
    // however ... it won't do to wrap the entire CQL query in double
    // quotes.
    // Without implementing a comprehensive CQL parser, we should NOT
    // enclose the
    // user input in quotes: leave it to the user.
    // if (!(qs.startsWith("\"") && qs.endsWith("\"")))
    // qs = '"' + qs + '"';
    request.setQuery(qs);

    request.setStartRecord(new PositiveInteger(Integer.toString(offset + 1)));
    request.setMaximumRecords(new NonNegativeInteger(Integer.toString(perpage)));
    request.setRecordPacking("xml");

    if (!Check.isEmpty(settings.getSchemaId())) {
      request.setRecordSchema(settings.getSchemaId());
    }

    return request;
  }

  @Override
  public GenericRecord getRecord(FederatedSearch srwSearch, String qs, int index) {
    try {
      // Setup Search
      SRWSettings settings = getSettings(srwSearch);
      SRWPort port = setupPort(settings);
      SearchRetrieveRequestType request = setupRequest(qs, index, 1, settings);

      // Do Search
      SearchRetrieveResponseType response = port.searchRetrieveOperation(request);
      RecordsType records = response.getRecords();

      // Show any problems with the search.
      String diagnosticMessage = doDiagnostic(response);

      // Build Results List
      RecordType[] record = records.getRecord();
      if (record.length > 0) {
        return convertRecordToGenericRecord(record[0]);
      } else if (!Check.isEmpty(diagnosticMessage)) {
        throw new RuntimeException(diagnosticMessage);
      }
      // else ... what is there ...?
      return null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private SRWSettings getSettings(FederatedSearch srwSearch) {
    SRWSettings settings = new SRWSettings();
    settings.load(srwSearch);
    return settings;
  }

  @Inject
  public void setXsltService(XsltService xsltService) {
    this.xsltService = xsltService;
    try {
      this.xsltService.cacheXslt(
          Resources.toString(getClass().getResource("MARC21slim2MODS3-3.xsl"), Charsets.UTF_8));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * EQUELLA supports just a handful of 'recordSchema' values, preferably whereby the EQUELLA known
   * long-form matches the recordSchema (expressed as a full URI) returned in the SRW server
   * response data, failing that the EQUELLA short-form matches, or failing that, the EQUELLA known
   * long- form matches the namespaceURI in the SRW server response data.<br>
   * Noting that the known long-form URI May be a slight abbreviation, hence startsWith is the
   * appropriate matching comparator, instead of equals.
   *
   * @param uriPair
   * @param recordSchema
   * @param namespaceURI
   * @return
   */
  private boolean matchesEither(
      Pair<String, String> uriPair, String recordSchema, String namespaceURI) {
    // @formatter:off
    return (recordSchema != null
            && (recordSchema.startsWith(uriPair.getFirst())
                || recordSchema.equals(uriPair.getSecond()))
        || (namespaceURI != null && namespaceURI.startsWith(uriPair.getFirst())));
    // @formatter:on
  }
}
