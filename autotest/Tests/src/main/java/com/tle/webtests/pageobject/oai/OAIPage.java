package com.tle.webtests.pageobject.oai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ORG.oclc.oai.harvester2.verb.GetRecord;
import ORG.oclc.oai.harvester2.verb.Identify;
import ORG.oclc.oai.harvester2.verb.ListIdentifiers;
import ORG.oclc.oai.harvester2.verb.ListMetadataFormats;
import ORG.oclc.oai.harvester2.verb.ListRecords;
import ORG.oclc.oai.harvester2.verb.ListSets;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;

public class OAIPage
{
	private final String baseUrl;

	public OAIPage(PageContext context, boolean oldEndpoint)
	{
		if( oldEndpoint )
		{
			baseUrl = context.getBaseUrl() + "oai";
		}
		else
		{
			baseUrl = context.getBaseUrl() + "p/oai";
		}
	}

	public OAIPage(PageContext context)
	{
		this(context, false);
	}

	public String identify() throws Exception
	{
		Identify identify = new Identify(baseUrl);
		return identify.getProtocolVersion();
	}

	public List<String> listMetadataFormats() throws Exception
	{
		ArrayList<String> formats = new ArrayList<String>();
		String string = new ListMetadataFormats(baseUrl).toString();
		PropBagThoroughIterator iterator = new PropBagEx(string).iterateAll("ListMetadataFormats/metadataFormat");
		while( iterator.hasNext() )
		{
			formats.add(iterator.next().getNode("metadataPrefix"));
		}
		return formats;
	}

	public Map<String, String> listResults(String format) throws Exception
	{
		return listResults(format, "");
	}
	
	public Map<String, String> listResults(String format, String set) throws Exception
	{
		HashMap<String, String> results = new HashMap<String, String>();
		ListRecords listRecords = new ListRecords(baseUrl, "", "", set, format);
		PropBagEx records = new PropBagEx(listRecords.toString());

		PropBagThoroughIterator iterator = records.iterateAll("ListRecords/record");
		while( iterator.hasNext() )
		{
			PropBagEx next = iterator.next();
			String key = next.getNode("header/identifier").trim();
			if( results.containsKey(key) )
			{
				throw new Error(key + " was already in the results: " + results);
			}
			results.put(key, next.getNode("metadata/xml/item/name").trim());
		}

		while( !Check.isEmpty(listRecords.getResumptionToken()) )
		{
			listRecords = new ListRecords(baseUrl, listRecords.getResumptionToken());
			records = new PropBagEx(listRecords.toString());
			iterator = records.iterateAll("ListRecords/record");
			while( iterator.hasNext() )
			{
				PropBagEx next = iterator.next();
				String key = next.getNode("header/identifier").trim();
				if( results.containsKey(key) )
				{
					throw new Error(key + " was already in the results: " + results);
				}
				results.put(key, next.getNode("metadata/xml/item/name").trim());
			}
		}

		return results;
	}

	public List<String> listIdentifiers(String format) throws Exception
	{
		return listIdentifiers(format, "");
	}

	public List<String> listIdentifiers(String format, String set) throws Exception
	{
		ArrayList<String> formats = new ArrayList<String>();
		ListIdentifiers listIdentifiers = new ListIdentifiers(baseUrl, "", "", set, format);
		PropBagEx records = new PropBagEx(listIdentifiers.toString());

		PropBagThoroughIterator iterator = records.iterateAll("ListIdentifiers/header");
		while( iterator.hasNext() )
		{
			formats.add(iterator.next().getNode("identifier"));
		}

		while( !Check.isEmpty(listIdentifiers.getResumptionToken()) )
		{
			listIdentifiers = new ListIdentifiers(baseUrl, listIdentifiers.getResumptionToken());
			records = new PropBagEx(listIdentifiers.toString());
			iterator = records.iterateAll("ListIdentifiers/header");
			while( iterator.hasNext() )
			{
				formats.add(iterator.next().getNode("identifier"));
			}
		}

		return formats;
	}

	public List<String> listSets() throws Exception
	{
		ArrayList<String> sets = new ArrayList<String>();
		PropBagEx setList = new PropBagEx(new ListSets(baseUrl).toString());

		PropBagThoroughIterator iterator = setList.iterateAll("ListSets/set");
		while( iterator.hasNext() )
		{
			sets.add(iterator.next().getNode("setName"));
		}
		return sets;
	}

	public PropBagEx getRecord(String format, String identifier) throws Exception
	{
		PropBagEx record = new PropBagEx(new GetRecord(baseUrl, identifier, format).toString());
		return record.getSubtree("GetRecord/record/metadata/xml");
	}

}
