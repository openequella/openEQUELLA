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

package com.tle.core.remoterepo.sru.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.SRUSettings;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.core.fedsearch.GenericRecord;
import com.tle.core.fedsearch.impl.BasicRecord;
import com.tle.core.fedsearch.impl.NullRecord;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.parser.mods.impl.loose.LooseModsRecord;
import com.tle.core.remoterepo.sru.service.SruService;
import com.tle.core.remoterepo.srw.service.impl.SrwServiceImpl;
import com.tle.core.xml.XmlDocument;
import com.tle.core.xml.XmlDocument.NodeListIterable;
import com.tle.core.xslt.service.XsltService;

/**
 * @author larry
 */
@Singleton
@Bind(SruService.class)
@SuppressWarnings("nls")
public class SruServiceImpl implements SruService
{
	private XsltService xsltService;

	@Inject
	public void setXsltService(XsltService xsltService)
	{
		this.xsltService = xsltService;
		try
		{
			URL srwTransformUrl = SrwServiceImpl.class.getResource("MARC21slim2MODS3-3.xsl");
			this.xsltService.cacheXslt(Resources.toString(srwTransformUrl, Charsets.UTF_8));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see com.tle.core.remoterepo.sru.service.SrwService#search(com.tle.beans.entity.FederatedSearch,
	 *      java.lang.String, int, int)
	 */
	@Override
	public SruSearchResults search(FederatedSearch sruSearch, String query, int offset, int perpage)
	{
		List<SruSearchResult> results = new ArrayList<SruSearchResult>();
		if( Check.isEmpty(query) )
		{
			return new SruSearchResults(results, 0, 0, 0);
		}

		SRUSettings settings = getSettings(sruSearch);

		HttpMethod httpMethod = null;

		try
		{
			httpMethod = executeQuery(sruSearch, query, settings, offset, perpage);
		}
		catch( Exception ex )
		{
			String errorMessage = ex.getMessage();
			Throwable thrown = ex.getCause();

			if( thrown != null )
			{
				String localizedMsg = thrown.getLocalizedMessage();
				if( !errorMessage.equalsIgnoreCase(localizedMsg) )
				{
					errorMessage += ": " + localizedMsg;
				}
			}

			errorMessage += " - " + settings.getUrl();
			SruSearchResults returnResults = new SruSearchResults(results, 0, 0, 0);
			returnResults.setErrorMessage(ex.getLocalizedMessage());
			return returnResults;
		}

		int theCode = -1;
		String responseAsString = null;
		InputStream responseInputStream = null;
		if( httpMethod != null )
		{
			theCode = httpMethod.getStatusCode();
			try
			{
				responseAsString = httpMethod.getResponseBodyAsString();
				responseInputStream = httpMethod.getResponseBodyAsStream();
			}
			catch( IOException ioe )
			{
				// ignore any response reading errors here
			}
		}

		int count = 0, available = 0;

		if( responseInputStream != null )
		{
			String diagnosticMessage = null;
			XmlDocument responseXmlDoc = null;
			// Root node is searchRetrieveResponse.
			try
			{
				responseXmlDoc = new XmlDocument(responseInputStream);
			}
			catch( Exception e )
			{
				try
				{
					responseXmlDoc = new XmlDocument(responseAsString);
				}
				catch( Exception inner )

				{
					diagnosticMessage = inner.getLocalizedMessage() + '\n' + responseAsString;
				}
			}
			// is there a diagnostic report? (which can co-exist with valid
			// records...?)

			if( responseXmlDoc != null )

			{
				diagnosticMessage = doDiagnostic(responseXmlDoc);
				try
				{
					String numRecordsStr = responseXmlDoc.nodeValue("/searchRetrieveResponse/numberOfRecords");
					if( !Check.isEmpty(numRecordsStr) )
					{
						available = Integer.parseInt(numRecordsStr);
					}
				}
				catch( NumberFormatException nfe )
				{
					// leave 'available' as 0
				}
			}
			count = (perpage > available ? available : perpage);

			// If we had an indication of results being present, process them;
			// otherwise if there's any substance to the diagnostic message,
			// set it as the return result's error message. In other words,
			// ignore the diagnostic if there're any results to to digest.
			if( responseXmlDoc != null && available > 0 )
			{
				results = buildResults(responseXmlDoc, offset, responseAsString, settings.getSchemaId());
			}
			else if( !Check.isEmpty(diagnosticMessage) )
			{
				SruSearchResults returnResult = new SruSearchResults(results, count, offset, available);

				returnResult.setErrorMessage(diagnosticMessage);
				return returnResult;
			}
			else
			{
				// did we ever get a http code, other than success (with empty
				// result set)?
				if( theCode != -1 && theCode != 200 )
				{
					responseAsString = "HTTP response code: " + theCode + responseAsString;
					SruSearchResults returnResult = new SruSearchResults(results, count, offset, available);

					returnResult.setErrorMessage(responseAsString);
					return returnResult;
				}
			}
		}

		return new SruSearchResults(results, count, offset, available);
	}

	/**
	 * Get a single record, presumably as identified in the list of results in
	 * search ...
	 * 
	 * @see com.tle.core.remoterepo.sru.service.SruService#getRecord(com.tle.beans.entity.FederatedSearch,
	 *      java.lang.String, int)
	 */
	@Override
	public GenericRecord getRecord(FederatedSearch sruSearch, String qs, int index)
	{
		GenericRecord ret = new NullRecord();
		SRUSettings settings = getSettings(sruSearch);
		String recordSchema = settings.getSchemaId();

		HttpMethod httpMethod = null;
		try
		{
			httpMethod = this.executeQuery(sruSearch, qs, settings, index, 1);
		}
		catch( Exception ex ) // Having got a group of results, we don't expect
								// an error when we retrieve a single result
		{
			throw new RuntimeException(ex);
		}

		InputStream responseInputStream = null;
		String responseInputString = null;
		if( httpMethod != null )
		{
			try
			{
				responseInputStream = httpMethod.getResponseBodyAsStream();
				responseInputString = httpMethod.getResponseBodyAsString();
			}
			catch( IOException ioe )
			{
				// ignore any response reading errors here
			}
		}

		PropBagEx responsePropBag = null;
		try
		{
			responsePropBag = new PropBagEx(responseInputStream);
		}
		catch( Exception sxpe ) // SAXParseException: premature EOF ...? !
		{
			responsePropBag = new PropBagEx(responseInputString);
		}

		PropBagEx theOneResponse = responsePropBag.getSubtree("records/record/recordData");

		if( theOneResponse != null )
		{
			NodeList dataNodes = theOneResponse.getRootElement().getChildNodes();
			for( int i = 0; i < dataNodes.getLength(); ++i )
			{
				Node dataNode = dataNodes.item(i);
				String strtrf = dataNode.getNodeName();
				if( strtrf != null )
				{
					theOneResponse = new PropBagEx(dataNode);
					break;
				}
			}

			if( matchesEither(MARCXML, recordSchema, null) )
			{
				ret = new LooseModsRecord(transformMarcToMods(theOneResponse));
			}
			else if( matchesEither(DC, recordSchema, null) )
			{
				// TODO: a DC parser
				BasicRecord basic = new BasicRecord();
				basic.setXml(theOneResponse);
				basic.setTitle(theOneResponse.getNode("title"));
				basic.setDescription(theOneResponse.getNode("description"));
				basic.setUrl(theOneResponse.getNode("identifier"));
				ret = basic;
			}
			else if( matchesEither(LOM, recordSchema, null) )
			{
				BasicRecord basic = new BasicRecord();
				basic.setXml(theOneResponse);
				String title = theOneResponse.getNode("general/title/string");
				String description = theOneResponse.getNode("general/description/string");
				if( Check.isEmpty(title) && Check.isEmpty(description) )
				{
					// if the string element ending the xpath isn't productive
					// try langstring.
					title = theOneResponse.getNode("general/title/langstring");
					description = theOneResponse.getNode("general/description/langstring");
				}
				basic.setTitle(title);
				basic.setDescription(description);
				basic.setUrl(theOneResponse.getNode("technical/location"));
				ret = basic;
			}
			else if( matchesEither(MODS, recordSchema, null) )
			{
				ret = new LooseModsRecord(theOneResponse);
			}
			else if( matchesEither(TLE, recordSchema, null) )
			{
				BasicRecord basic = new BasicRecord();
				basic.setXml(theOneResponse);
				basic.setTitle(theOneResponse.getNode("item/name"));
				basic.setDescription(theOneResponse.getNode("item/description"));
				basic.setUrl(theOneResponse.getNode("institutionUrl") + "items/" + theOneResponse.getNode("item/@uuid")
					+ "/" + theOneResponse.getNode("item/@version"));
				ret = basic;
			}
		}
		return ret;
	}

	private HttpMethod executeQuery(FederatedSearch sruSearch, String query, SRUSettings settings, int offset,
		int perpage) throws IOException
	{
		HttpMethod httpMethod = null;

		try
		{
			// URL includes the port (if any) we trust ...
			URL url = new URL(settings.getUrl());
			PostMethod postMethod = new PostMethod(url.toExternalForm());
			NameValuePair[] nameValuePairs = populateNameValuePairs(query, settings, offset, perpage);
			postMethod.addParameters(nameValuePairs);
			httpMethod = postMethod;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}

		HttpClient httpClient = new HttpClient();
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(sruSearch.getTimeout() * 1000);
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(sruSearch.getTimeout() * 1000);
		// Prevent the default 3 tries - so once is enough ...?
		httpClient.getHttpConnectionManager().getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
			new DefaultHttpMethodRetryHandler(0, false));

		httpClient.executeMethod(httpMethod);

		return httpMethod;
	}

	private SRUSettings getSettings(FederatedSearch sruSearch)
	{
		SRUSettings settings = new SRUSettings();
		settings.load(sruSearch);
		return settings;
	}

	private NameValuePair[] populateNameValuePairs(String query, SRUSettings settings, int offset, int perpage)
	{
		List<NameValuePair> nameValueArray = new ArrayList<NameValuePair>();
		nameValueArray.add(new NameValuePair(SRU_VERSION, SRU_VERSION_1_1));
		nameValueArray.add(new NameValuePair(SRU_OPERATION, SRU_SEARCH_RETRIEVE));

		// encode the query string (which in theory could be a complex string
		// using CQL syntax
		nameValueArray.add(new NameValuePair(SRU_QUERY, query));

		// From parameter offset (zero-based index value), add the startRecord
		// parameter, incrementing by 1 to get ordinal value. Any negative
		// value we ignore. We can assume any SRU server's default is '1'.
		if( offset >= 0 )
		{
			nameValueArray.add(new NameValuePair(SRU_START_RECORD, Integer.toString(offset + 1)));
		}

		// Maximum records to be returned (per page for example)
		if( perpage > 0 )
		{
			nameValueArray.add(new NameValuePair(SRU_MAXIMUM_RECORDS, Integer.toString(perpage)));
		}

		// Do we have a recordSchema specified? Things could get very tricky if
		// we don't; Without an explicit 'recordSchema' value in the request
		// URL, the response will come back in whatever format is the default
		// for the server, but without a <recordSchema> node in the response
		// body to tell us what it is. (The only fallback from that point is to
		// look inside the child node of recordData, and look for its namespace
		// URI).
		String specifiedSchemaId = settings.getSchemaId();
		if( !Check.isEmpty(specifiedSchemaId) )
		{
			nameValueArray.add(new NameValuePair(SRU_RECORD_SCHEMA, specifiedSchemaId));
		}

		return nameValueArray.toArray(new NameValuePair[nameValueArray.size()]);
	}

	private String doDiagnostic(XmlDocument response)
	{
		StringBuilder diagnosticMessage = new StringBuilder();
		NodeListIterable nodeList = response.nodeList("/searchRetrieveResponse/diagnostics/diagnostic");
		int diagnosticNum = nodeList.size();
		if( diagnosticNum > 0 )
		{
			for( Node thisNode : nodeList )
			{
				String thisDiagnostic = doDiagnostic(thisNode);// thisNode.getTextContent();
				if( diagnosticMessage.length() > 0 )
				{
					diagnosticMessage.append('\n');
				}
				diagnosticMessage.append(thisDiagnostic);
			}
		}
		return diagnosticMessage.toString();
	}

	private List<SruSearchResult> buildResults(XmlDocument responseXmlDoc, int offset, String responseAsString,
		String schemaInSettings)
	{
		List<SruSearchResult> results = new ArrayList<SruSearchResult>();
		int index = offset;

		for( Node singleRecord : responseXmlDoc.nodeList("/searchRetrieveResponse/records/record/recordData") )
		{
			if( singleRecord != null ) // surely not
			{
				results.add(convertNodeRecordToSearchResult(singleRecord, index, responseAsString, schemaInSettings));
			}
			index++;
		}
		return results;
	}

	private SruSearchResult convertNodeRecordToSearchResult(Node singleRecordNode, int index, String responseAsString,
		String schemaInSettings)
	{
		SruSearchResult result = new SruSearchResult(index);
		// FIXME: English string
		String title = "Unknown";
		String description = "";
		String surl = null;
		String recordSchema = schemaInSettings;
		if( Check.isEmpty(recordSchema) )
		{
			NodeList recordChildren = singleRecordNode.getParentNode().getChildNodes();
			for( int i = 0; i < recordChildren.getLength(); ++i )
			{
				Node childNode = recordChildren.item(i);
				String siblingNodeName = childNode.getNodeName();
				if( !Check.isEmpty(siblingNodeName) && siblingNodeName.endsWith("recordSchema") )
				{
					recordSchema = childNode.getNodeValue();
					break;
				}
			}
		}

		String namespaceUri = singleRecordNode.getNamespaceURI();

		if( Check.isEmpty(recordSchema) && Check.isEmpty(namespaceUri) )
		{
			namespaceUri = findNamespaceUriFromRecordNode(responseAsString);
		}

		String rootElemName = singleRecordNode.getNodeName();
		if( rootElemName.endsWith("diagnostic") )
		{
			doDiagnostic(singleRecordNode);
		}

		PropBagEx singleRecordPropBag = null;
		NodeList dataNodes = singleRecordNode.getChildNodes();
		for( int i = 0; i < dataNodes.getLength(); ++i )
		{
			Node dataNode = dataNodes.item(i);
			String nodeName = dataNode.getNodeName();
			// Depending on the schema, the child of a recordData node could
			// have any sort of name.
			// Skip text nodes - see EQ-385
			if( !Check.isEmpty(nodeName) && dataNode.getNodeType() != Node.TEXT_NODE )
			{
				singleRecordPropBag = new PropBagEx(dataNode);
				break;
			}
		}

		if( singleRecordPropBag != null )
		{
			if( singleRecordPropBag.getRootElement().getNodeName().endsWith("diagnostic") )
			{
				doDiagnostic(singleRecordNode);
			}
			else if( matchesEither(MARCXML, recordSchema, namespaceUri) )
			{
				LooseModsRecord mods = new LooseModsRecord(transformMarcToMods(singleRecordPropBag));
				title = mods.getTitle();
				description = mods.getDescription();
				surl = mods.getUrl();
			}
			else if( matchesEither(DC, recordSchema, namespaceUri) )
			{
				title = singleRecordPropBag.getNode("title");
				description = singleRecordPropBag.getNode("description");
				surl = singleRecordPropBag.getNode("identifier");
				if( surl.length() == 0 )
				{
					surl = null;
				}
			}
			else if( matchesEither(LOM, recordSchema, namespaceUri) )
			{
				title = singleRecordPropBag.getNode("general/title/string");
				description = singleRecordPropBag.getNode("general/description/string");
				if( Check.isEmpty(title) && Check.isEmpty(description) )
				{
					// if the string element ending the xpath isn't productive
					// try langstring.
					title = singleRecordPropBag.getNode("general/title/langstring");
					description = singleRecordPropBag.getNode("general/description/langstring");
				}
				surl = singleRecordPropBag.getNode("technical/location");
				if( surl.length() == 0 )
				{
					surl = null;
				}
			}
			else if( matchesEither(MODS, recordSchema, namespaceUri) )
			{
				LooseModsRecord mods = new LooseModsRecord(singleRecordPropBag);
				title = mods.getTitle();
				description = mods.getDescription();
				surl = mods.getUrl();
			}
			else if( matchesEither(TLE, recordSchema, namespaceUri) )
			{
				title = singleRecordPropBag.getNode("item/name");
				description = singleRecordPropBag.getNode("item/description");
				surl = singleRecordPropBag.getNode("institutionUrl") + "items/"
					+ singleRecordPropBag.getNode("item/@uuid") + "/" + singleRecordPropBag.getNode("item/@version");
			}
		}

		result.setTitle(title);
		result.setDescription(description == null ? "" : description);
		result.setUrl(surl);
		return result;
	}

	private String doDiagnostic(Node rootNode)
	{
		NodeList nodeList = rootNode.getChildNodes();
		String uri = null, details = null, message = null;
		for( int i = 0; i < nodeList.getLength(); ++i )
		{
			Node node = nodeList.item(i);
			String nodnm = node.getNodeName();
			if( !Check.isEmpty(nodnm) )
			{
				if( nodnm.endsWith("uri") )
				{
					uri = node.getTextContent();
				}
				else if( nodnm.endsWith("details") )
				{
					details = node.getTextContent();
				}
				else if( nodnm.endsWith("message") )
				{
					message = node.getTextContent();
				}
			}
		}

		String wholeMsg = null;
		if( !Check.isEmpty(details) )
		{
			wholeMsg = "details: " + details;
		}

		if( !Check.isEmpty(message) )
		{
			if( wholeMsg == null )
			{
				wholeMsg = "";
			}
			else if( wholeMsg.length() > 0 )
			{
				wholeMsg += ", ";
			}
			wholeMsg = wholeMsg + "message: " + message;
		}

		if( uri != null && uri.length() > 0 )
		{
			if( wholeMsg == null )
			{
				wholeMsg = "";
			}
			else if( wholeMsg.length() > 0 )
			{
				wholeMsg += ", ";
			}
			wholeMsg = wholeMsg + "uri: " + uri;
		}

		if( Check.isEmpty(wholeMsg) )
		{
			wholeMsg = "details unspecified";
		}

		return wholeMsg;
	}

	private PropBagEx transformMarcToMods(PropBagEx xml)
	{
		try
		{
			String xslt = Resources.toString(getClass().getResource("MARC21slim2MODS3-3.xsl"), Charsets.UTF_8);
			String transXml = xsltService.transformFromXsltString(xslt, xml);
			return new PropBagEx(transXml);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * EQUELLA supports just a handful of 'recordSchema' values, preferably
	 * whereby the EQUELLA known long-form matches the recordSchema (expressed
	 * as a full URI) returned in the SRU/W server response data, failing that
	 * the EQUELLA short-form matches, or failing that, the EQUELLA known long-
	 * form matches the namespaceURI in the SRU/W server response data.<br />
	 * Noting that the known long-form URI May be a slight abbreviation, hence
	 * startsWith is the appropriate matching comparator, instead of equals.
	 * 
	 * @param uriPair
	 * @param recordSchema
	 * @param namespaceURI
	 * @return
	 */
	private boolean matchesEither(Pair<String, String> uriPair, String recordSchema, String namespaceURI)
	{
		//@formatter:off
		return (
			(!Check.isEmpty(recordSchema) &&
				(recordSchema.startsWith(uriPair.getFirst()) || recordSchema.equals(uriPair.getSecond()))
			) ||
			(!Check.isEmpty(namespaceURI) && namespaceURI.startsWith(uriPair.getFirst())));
		//@formatter:on
	}

	/**
	 * If the returned xml for the SRU server doesn't conveniently state the
	 * record schema in a dedicated text node, then the only way to determine
	 * the response schema is to look for a namespaceUri affixed to the record
	 * nodes within the recordData elements. Depending on the schema, the child
	 * of a recordData node could have any sort of name.
	 * 
	 * @param rawXml
	 * @return
	 */
	private String findNamespaceUriFromRecordNode(String rawXml)
	{
		XmlDocument xmel = new XmlDocument(rawXml); // inc namespace ... ?
		NodeList firstGen = xmel.getDOMDocument().getElementsByTagNameNS("*", "recordData");
		String recordNamespaceUri = null;
		outhere : for( int i = 0; i < firstGen.getLength(); ++i )
		{
			Node n = firstGen.item(i);
			NodeList recordGen = n.getChildNodes();
			for( int recordIndex = 0; recordIndex < recordGen.getLength(); ++recordIndex )
			{
				Node recordNode = recordGen.item(i);
				String nodeName = recordNode.getNodeName();
				if( !Check.isEmpty(nodeName) )
				{
					recordNamespaceUri = recordNode.getNamespaceURI();
					break outhere;
				}
			}
		}
		return recordNamespaceUri;
	}
}