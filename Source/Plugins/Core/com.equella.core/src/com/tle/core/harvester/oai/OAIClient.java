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

package com.tle.core.harvester.oai;

import java.net.MalformedURLException;
import java.net.URL;

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

	public OAIClient(String host, int port, String path) throws MalformedURLException
	{
		url = new URL("http", host, port, path);
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
