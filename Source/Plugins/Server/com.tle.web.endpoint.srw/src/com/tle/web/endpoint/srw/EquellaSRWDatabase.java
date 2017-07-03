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

package com.tle.web.endpoint.srw;

import ORG.oclc.os.SRW.*;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.service.ItemService;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.services.item.FreetextSearchResults;
import gov.loc.www.zing.srw.ExtraDataType;
import gov.loc.www.zing.srw.ScanRequestType;
import gov.loc.www.zing.srw.ScanResponseType;
import gov.loc.www.zing.srw.SearchRetrieveRequestType;
import gov.loc.www.zing.srw.TermType;
import gov.loc.www.zing.srw.TermsType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.axis.types.PositiveInteger;
import org.apache.log4j.Logger;
import org.z3950.zing.cql.CQLTermNode;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.Schema;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.Utils;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Search;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class EquellaSRWDatabase extends SRWDatabase
{
	private static final Logger LOGGER = Logger.getLogger(EquellaSRWDatabase.class);
	private static final ThreadLocal<String> EXPLAIN = new ThreadLocal<String>();

	protected static final Map<String, SchemaInfo> SCHEMAS = new HashMap<String, SchemaInfo>();
	static
	{
		SCHEMAS.put("OAI_DC", new SchemaInfo("OAI_DC", "info:srw/schema/1/dc-v1.1", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			"http://www.loc.gov/srw/dc-schema.xsd", "dc", "Dublin Core")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

		SCHEMAS.put("OAI_LOM", new SchemaInfo("OAI_LOM", "http://ltsc.ieee.org/xsd/LOMv1p0", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			"http://www.rdn.ac.uk/oai/lom/lom.xsd", "LOM", "LOM")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

		SCHEMAS.put("tle", new SchemaInfo("tle", "http://www.thelearningedge.com.au/xsd/item", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			"http://www.thelearningedge.com.au/xsd/item", "tle", "TLE")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

	// Package protected
	static final SchemaInfo DEFAULT_SCHEMA = SCHEMAS.get("tle"); //$NON-NLS-1$

	@Inject
	private FreeTextService freeTextService;
	@Inject
	private ItemService itemService;
	@Inject
	private SchemaService schemaService;
	@Inject
	private ItemHelper itemHelper;

	@Override
	public String getExtraResponseData(QueryResult result, SearchRetrieveRequestType request)
	{
		// This needs to be null or an error is thrown
		return null;
	}

	@Override
	public QueryResult getQueryResult(String query, SearchRetrieveRequestType request) throws InstantiationException
	{
		DefaultSearch search = new DefaultSearch();
		search.setQuery(query);
		int startPoint = 1;
		PositiveInteger startRec = request.getStartRecord();
		if( startRec != null )
		{
			startPoint = startRec.intValue();
		}

		int count = 1;
		NonNegativeInteger max = request.getMaximumRecords();
		if( max != null )
		{
			count = max.intValue();
		}

		return new TLEQueryResult(freeTextService.search(search, startPoint - 1, count));
	}

	@Override
	public void addRenderer(String schemaName, String schemaID, Properties props) throws InstantiationException
	{
		// Nothing to see here, move along...
	}

	@Override
	public boolean hasaConfigurationFile()
	{
		return false;
	}

	@Override
	public ScanResponseType doRequest(ScanRequestType type) throws ServletException
	{
		DefaultSearch search = new DefaultSearch();
		String clause = type.getScanClause();
		search.setQuery(clause);
		List<Search> filters = new ArrayList<Search>();
		filters.add(search);
		int[] counts = freeTextService.countsFromFilters(filters);

		ScanResponseType scanResponse = new ScanResponseType();
		TermsType terms = new TermsType();
		TermType term[] = new TermType[0];
		TermType t = new TermType();
		t.setNumberOfRecords(new NonNegativeInteger(Integer.toString(counts[0])));
		t.setValue(clause);
		term[0] = t;
		terms.setTerm(term);
		scanResponse.setTerms(terms);
		return scanResponse;
	}

	@Override
	public String getIndexInfo()
	{
		// Nothing to see here, move along...
		return ""; //$NON-NLS-1$
	}

	// TODO: This should be implemented. See TLERecordIterator.nextRecord
	@Override
	public Record transform(Record rec, String schemaID) throws SRWDiagnostic
	{
		return rec;
	}

	@Override
	public String getSchemaID(String schemaName)
	{
		SchemaInfo schema = SCHEMAS.get(schemaName);
		return schema == null ? null : schema.getIdentifier();
	}

	@Override
	public boolean supportsSort()
	{
		return false;
	}

	private class TLEQueryResult extends QueryResult
	{
		private final FreetextSearchResults<?> searchResults;

		public TLEQueryResult(FreetextSearchResults<?> searchResults)
		{
			this.searchResults = searchResults;
		}

		/**
		 * I believe this is supposed to be the 'available' count
		 */
		@Override
		public long getNumberOfRecords()
		{
			// return Math.min(max, searchResults.getCount());
			return searchResults.getAvailable();
		}

		@Override
		public RecordIterator newRecordIterator(long startPoint, int numResults, String schemaID, ExtraDataType arg3)
			throws InstantiationException
		{
			return new TLERecordIterator(searchResults, schemaID);
		}
	}

	@Override
	public TermList getTermList(CQLTermNode arg0, int arg1, int arg2, ScanRequestType arg3)
	{
		return null;
	}

	@Override
	public String getSchemaInfo()
	{
		List<String> exportSchemaTypes = schemaService.getExportSchemaTypes();
		StringBuffer schemaInfoBuf = new StringBuffer("<schemaInfo>\n"); //$NON-NLS-1$

		for( String schemaName : exportSchemaTypes )
		{
			SchemaInfo info = SCHEMAS.get(schemaName);
			if( info != null )
			{
				try
				{
					schema(schemaInfoBuf, info);
				}
				catch( Exception e )
				{
					LOGGER.error(e, e);
				}
			}
		}
		schema(schemaInfoBuf, DEFAULT_SCHEMA);

		schemaInfoBuf.append("</schemaInfo>\n"); //$NON-NLS-1$

		return schemaInfoBuf.toString();
	}

	private void schema(StringBuffer schemaInfoBuf, SchemaInfo info)
	{
		schemaInfoBuf.append("<schema identifier=\""); //$NON-NLS-1$
		schemaInfoBuf.append(Utils.ent(info.getIdentifier()));
		schemaInfoBuf.append("\" schemaLocation=\""); //$NON-NLS-1$
		schemaInfoBuf.append(Utils.ent(info.getLocation()));
		schemaInfoBuf.append("\" sort=\"false\" retrieve=\"true\" name=\""); //$NON-NLS-1$
		schemaInfoBuf.append(Utils.ent(info.getName()));
		schemaInfoBuf.append("\"><title>"); //$NON-NLS-1$
		schemaInfoBuf.append(Utils.ent(info.getTitle()));
		schemaInfoBuf.append("</title> </schema>"); //$NON-NLS-1$
	}

	@Override
	public void setExplainRecord(String ex)
	{
		EXPLAIN.set(ex);
	}

	@Override
	public String getExplainRecord(HttpServletRequest request)
	{
		// This is so it can be generated at run time because we may change the
		// available schemas
		String ex = EXPLAIN.get();
		if( ex == null )
		{
			makeExplainRecord(request);
			ex = EXPLAIN.get();
		}
		EXPLAIN.remove();
		return ex;
	}

	private class TLERecordIterator implements RecordIterator
	{
		private final FreetextSearchResults<?> searchResults;

		private final String schemaID;

		private int i;

		public TLERecordIterator(FreetextSearchResults<?> searchResults, String schemaID)
		{
			this.searchResults = searchResults;
			this.schemaID = schemaID;
			this.i = 0;
		}

		@Override
		public void close()
		{
			// IGNORE
		}

		// TODO: This shouldn't be required anymore, there is a list of map of
		// transformers in the SRWDatabase base class that you can add to
		@Override
		public Record nextRecord()
		{
			ItemPack<Item> pack = new ItemPack<>();
			pack.setItem(searchResults.getResults().get(i++));
			pack.setXml(itemService.getItemXmlPropBag(pack.getItem()));

			PropBagEx xml = itemHelper.convertToXml(pack);

			String s = null;
			if( schemaID != null )
			{
				String transformId = findTleSchemaId(schemaID);
				if( transformId != null )
				{
					final Schema schema = pack.getItem().getItemDefinition().getSchema();
					s = schemaService.transformForExport(schema.getId(), transformId, xml, true);
				}
			}

			String actualSchema = schemaID;
			if( s == null )
			{
				actualSchema = DEFAULT_SCHEMA.getIdentifier();
				s = xml.toString();
			}

			return new Record(s, actualSchema);

		}

		private String findTleSchemaId(String identifier)
		{
			for( SchemaInfo info : SCHEMAS.values() )
			{
				if( identifier.equals(info.getIdentifier()) )
				{
					return info.getTleId();
				}
			}
			return null;
		}

		@Override
		public boolean hasNext()
		{
			return i < searchResults.getCount();
		}

		@Override
		public Object next()
		{
			return nextRecord();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	// Package protected
	static class SchemaInfo
	{
		private final String tleId;
		private final String identifier;
		private final String location;
		private final String name;
		private final String title;

		public SchemaInfo(String tleId, String identifier, String location, String name, String title)
		{
			this.tleId = tleId;
			this.identifier = identifier;
			this.location = location;
			this.name = name;
			this.title = title;
		}

		public String getTleId()
		{
			return tleId;
		}

		public String getIdentifier()
		{
			return identifier;
		}

		public String getLocation()
		{
			return location;
		}

		public String getName()
		{
			return name;
		}

		public String getTitle()
		{
			return title;
		}
	}

	@Override
	public void init(String arg0, String arg1, String arg2, String arg3, Properties arg4) throws Exception 
	{
		// nothing		
	}
}
