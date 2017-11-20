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

package com.tle.core.harvester;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.dytech.devlib.Code;
import com.dytech.devlib.PropBagEx;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.common.searching.Search;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.oai.OAIClient;
import com.tle.core.harvester.oai.data.Header;
import com.tle.core.harvester.oai.data.List;
import com.tle.core.harvester.oai.data.Record;
import com.tle.core.harvester.oai.data.Repository;
import com.tle.core.harvester.oai.data.ResumptionToken;
import com.tle.core.harvester.oai.error.CannotDisseminateFormatException;
import com.tle.core.harvester.oai.error.IdDoesNotExistException;
import com.tle.core.harvester.oai.error.NoRecordsMatchException;
import com.tle.core.harvester.old.LearningObject;
import com.tle.core.harvester.old.OAIDublinCore;
import com.tle.core.harvester.search.HarvesterSearch;

@Bind
public class OAIProtocol extends AbstractHarvesterProtocol
{
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //$NON-NLS-1$
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	private static final String FULL_GRANULARITY = "YYYY-MM-DDThh:mm:ssZ"; //$NON-NLS-1$
	private static final Logger LOGGER = Logger.getLogger(OAIProtocol.class);

	private OAIClient client;
	private String format;
	private DateFormat dateFormat;
	private String set;

	@Override
	public int setupAndRun(HarvesterProfile aProfile, boolean testOnly) throws Exception
	{
		URL server = new URL(aProfile.getAttribute("server")); //$NON-NLS-1$

		client = new OAIClient(server.getHost(), server.getPort(), server.getPath());
		Repository repository = client.identify();

		if( repository.getGranularity().equals(FULL_GRANULARITY) )
		{
			dateFormat = TIME_FORMAT;
		}
		else
		{
			dateFormat = DATE_FORMAT;
		}

		format = aProfile.getAttribute("format"); //$NON-NLS-1$
		if( format == null || format.isEmpty() )
		{
			format = "oai_dc"; //$NON-NLS-1$
		}

		set = aProfile.getAttribute("aSet"); //$NON-NLS-1$
		if( set != null && set.isEmpty() )
		{
			set = null;
		}

		return super.setupAndRun(aProfile, testOnly);
	}

	@Override
	public Collection<LearningObject> getUpdatedLearningObjects(Date since) throws Exception
	{
		Collection<LearningObject> updated = new ArrayList<LearningObject>();
		try
		{
			List list = client.listIdentifiers(set, dateFormat.format(since), null, format);

			if( list != null )
			{
				addHeaders(updated, list);
			}
			else
			{
				throw new NoRecordsMatchException();
			}

			ResumptionToken token = list.getResumptionToken();
			while( token != null && token.getToken() != null && token.getToken().length() > 0 )
			{
				list = client.listIdentifiers(token);
				if( list != null )
				{
					addHeaders(updated, list);
					token = list.getResumptionToken();
				}
				else
				{
					token = null;
				}
			}
		}
		catch( NoRecordsMatchException e )
		{
			// Fair enough
		}

		return updated;
	}

	private void addHeaders(Collection<LearningObject> updated, List list)
	{
		for( Iterator<?> iter = list.iterator(); iter.hasNext(); )
		{
			Header header = (Header) iter.next();
			LearningObject convertHeaderToLO = convertHeaderToLO(header);
			if( convertHeaderToLO != null )
			{
				updated.add(convertHeaderToLO);
			}
		}
	}

	private LearningObject convertHeaderToLO(Header header)
	{
		String status = header.getStatus();
		if( status != null && status.equalsIgnoreCase("deleted") ) //$NON-NLS-1$
		{
			LOGGER.info(CurrentLocale.get("com.tle.core.harvester.log.oai.delete", header.getIdentifier())); //$NON-NLS-1$
			return null;
		}
		else
		{
			String id = header.getIdentifier();
			Date date = null;
			try
			{
				date = dateFormat.parse(header.getDatestamp());
			}
			catch( ParseException e )
			{
				date = Calendar.getInstance().getTime();
			}
			return new LearningObject(id, "", date, false); //$NON-NLS-1$
		}

	}

	@Override
	public Search getTLESearchRequest(LearningObject lobject)
	{
		HarvesterSearch search = new HarvesterSearch();

		FreeTextBooleanQuery ftquery = WhereParser.parse("WHERE /xml/item/oai/@id = '" //$NON-NLS-1$
			+ Code.SQL(lobject.getIdentifier()) + '\'');
		search.setFreeTextQuery(ftquery);

		return search;
	}

	@Override
	public String createAttachmentName(LearningObject lobject)
	{
		// No attachments to download
		return ""; //$NON-NLS-1$
	}

	protected void processMetadataToLO(Object o, PropBagEx xml)
	{
		PropBagEx element = new PropBagEx((Element) o);
		try
		{
			String xsltName = getProfile().getAttribute("schemaInputTransform"); //$NON-NLS-1$
			if( xsltName != null && !xsltName.isEmpty() )
			{
				xml.append("item/oai", element); //$NON-NLS-1$

				LOGGER.info(CurrentLocale.get(KEY_PFX + "log.transform.before")); //$NON-NLS-1$
				LOGGER.info(xml.toString());

				xml.setXML(transformSchema(xml, xsltName));

				LOGGER.info(CurrentLocale.get(KEY_PFX + "log.transform.after")); //$NON-NLS-1$
				LOGGER.info(xml.toString());
			}
			else
			{
				new OAIDublinCore().processMetadataToLO(element, xml);
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void postProcessing(PropBagEx xml, LearningObject lobject)
	{
		try
		{
			Record record = client.getRecord(lobject.getIdentifier(), format);
			Object o = record.getMetadata();
			processMetadataToLO(o, xml);
			xml.setNode("item/oai/@id", lobject.getIdentifier()); //$NON-NLS-1$
		}
		catch( CannotDisseminateFormatException e )
		{
			// This shouldn't happen, and if it does we probably want to abort
			// THIS upload
			throw new RuntimeException(e);
		}
		catch( IdDoesNotExistException e )
		{
			// This shouldn't happen, and if it does we probably want to abort
			// THIS upload
			throw new RuntimeException(e);
		}
	}

	@Override
	public void downloadLO(LearningObject lobject, String stagingID) throws Exception
	{
		// ignorez
	}

}
