/*
 * Created on Apr 11, 2005
 */
package com.tle.core.harvester.oai;

import java.net.MalformedURLException;
import java.net.URL;

import com.tle.common.Check;
import com.tle.core.harvester.oai.data.List;
import com.tle.core.harvester.oai.data.Record;
import com.tle.core.harvester.oai.data.Repository;
import com.tle.core.harvester.oai.data.ResumptionToken;
import com.tle.core.harvester.oai.error.CannotDisseminateFormatException;
import com.tle.core.harvester.oai.error.IdDoesNotExistException;
import com.tle.core.harvester.oai.error.NoMetadataFormatsException;
import com.tle.core.harvester.oai.error.NoRecordsMatchException;
import com.tle.core.harvester.oai.error.NoSetHierarchyException;
import com.tle.core.harvester.oai.verb.GetRecord;
import com.tle.core.harvester.oai.verb.Identify;
import com.tle.core.harvester.oai.verb.ListIdentifiers;
import com.tle.core.harvester.oai.verb.ListMetadataFormats;
import com.tle.core.harvester.oai.verb.ListRecords;
import com.tle.core.harvester.oai.verb.ListSets;
import com.tle.core.harvester.oai.verb.Verb;

/**
 * 
 */
public class OAIClient
{
	private URL url;

	public OAIClient(URL url)
	{
		this.url = url;
	}

	public OAIClient(String protocol, String host, int port, String path) throws MalformedURLException
	{
		// We don't require the protocol being explicit, but it's only polite to
		// ask, and accept what's given. Defaults to "http".
		if( Check.isEmpty(protocol) )
		{
			protocol = "http";
		}
		url = new URL(protocol, host, port, path);
	}

	// *********************** METHODS ***********************//

	private void setup(Verb verb)
	{
		verb.setURL(url);
	}

	public Repository identify()
	{
		Identify id = new Identify();
		setup(id);
		return id.getResult();
	}

	public List listMetadataFormats() throws IdDoesNotExistException, NoMetadataFormatsException
	{
		ListMetadataFormats verb = new ListMetadataFormats();
		setup(verb);
		return verb.getResult();
	}

	public List listMetadataFormats(String identifier) throws IdDoesNotExistException, NoMetadataFormatsException
	{
		ListMetadataFormats verb = new ListMetadataFormats(identifier);
		setup(verb);
		return verb.getResult();
	}

	public List listMetadataFormats(ResumptionToken token) throws IdDoesNotExistException, NoMetadataFormatsException
	{
		ListMetadataFormats verb = new ListMetadataFormats(token);
		setup(verb);
		return verb.getResult();
	}

	public List listIdentifiers(String set, String from, String until, String prefix) throws NoRecordsMatchException,
		NoSetHierarchyException, CannotDisseminateFormatException
	{
		ListIdentifiers verb = new ListIdentifiers(set, from, until, prefix);
		setup(verb);
		return verb.getResult();
	}

	public List listIdentifiers(ResumptionToken token) throws NoRecordsMatchException, NoSetHierarchyException
	{
		ListIdentifiers verb = new ListIdentifiers(token);
		setup(verb);
		try
		{
			return verb.getResult();
		}
		catch( CannotDisseminateFormatException e )
		{
			// Never happen
			throw new RuntimeException(e);
		}
	}

	public List listRecords(String set, String from, String until, String prefix) throws NoRecordsMatchException,
		NoSetHierarchyException, CannotDisseminateFormatException
	{
		ListRecords verb = new ListRecords(set, from, until, prefix);
		setup(verb);
		return verb.getResult();
	}

	public List listRecords(ResumptionToken token) throws NoRecordsMatchException, NoSetHierarchyException
	{
		ListRecords verb = new ListRecords(token);
		setup(verb);
		try
		{
			return verb.getResult();
		}
		catch( CannotDisseminateFormatException e )
		{
			// Never happen
			throw new RuntimeException(e);
		}
	}

	public Record getRecord(String id, String metadataPrefix) throws IdDoesNotExistException,
		CannotDisseminateFormatException
	{
		GetRecord verb = new GetRecord(id, metadataPrefix);
		setup(verb);
		return verb.getResult();
	}

	public List listSets()
	{
		ListSets verb = new ListSets();
		setup(verb);
		return verb.getResult();
	}

	public List listSets(ResumptionToken token)
	{
		ListSets verb = new ListSets(token);
		setup(verb);
		return verb.getResult();
	}
}
