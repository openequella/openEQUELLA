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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.thoughtworks.xstream.XStream;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.harvester.HarvesterException;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.searching.Search;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.old.LearningObject;
import com.tle.core.harvester.soap.SoapHarvesterService;
import com.tle.core.services.FileSystemService;
import com.tle.core.util.archive.ArchiveType;
import com.tle.web.remoting.soap.SoapClientFactory;

@Bind
@SuppressWarnings("nls")
public class EQUELLAProtocol extends AbstractHarvesterProtocol
{
	private static final Logger LOGGR = Logger.getLogger(EQUELLAProtocol.class);
	private static final String HARVESTER_END_POINT = "services/SoapHarvesterService";

	private final XStream customAttachXstream = new XStream();

	@Inject
	private SoapClientFactory clientFactory;
	@Inject
	private FileSystemService fileSystemService;

	private SoapHarvesterService soapService;
	private boolean liveOnly;

	public EQUELLAProtocol()
	{
		customAttachXstream.alias("attributes", Map.class);
	}

	@Override
	public int setupAndRun(HarvesterProfile aProfile, boolean testOnly) throws Exception
	{
		URL server = new URL(aProfile.getAttribute("server"));
		liveOnly = aProfile.getAttribute("liveOnly", true);
		final URL endpointUrl = new URL(server, HARVESTER_END_POINT);

		soapService = clientFactory.createSoapClient(SoapHarvesterService.class, endpointUrl,
			"http://soap.harvester.core.tle.com");

		String user = aProfile.getAttribute("user");

		try
		{
			if( user != null && !user.isEmpty() )
			{
				soapService.login(user, aProfile.getAttribute("pass"));
			}
			else
			{
				soapService.logout();
			}
		}
		catch( Exception ex )
		{
			LOGGR.error(CurrentLocale.get(KEY_PFX + "error.equella.setupandrun"), ex);
			// Don't send the cause the the admin console, it won't know what to
			// do with it
			throw new HarvesterException(ex.getMessage());
		}

		return super.setupAndRun(aProfile, testOnly);
	}

	@Override
	public Collection<LearningObject> getUpdatedLearningObjects(Date since) throws Exception
	{
		UtcDate date = new UtcDate(since);

		Collection<LearningObject> updated = new ArrayList<LearningObject>();

		PropBagEx resultsXml = null;

		String[] split = null;
		int length = 1;
		String collection = getProfile().getAttribute("collection");
		if( collection != null && !collection.isEmpty() )
		{
			split = collection.split(":");
			length = split.length;
		}
		else
		{
			return Collections.emptyList();
		}

		if( length == 1 )
		{
			resultsXml = new PropBagEx(soapService.searchItemsSince(split, liveOnly,
				date.format(Dates.ISO_WITH_TIMEZONE)));
		}
		else if( length >= 2 )
		{
			String dynaCol = split[1];
			String virtVal = (length == 2 ? "" : split[2]);
			resultsXml = new PropBagEx(soapService.searchDynamicCollection(dynaCol, virtVal,
				date.format(Dates.ISO_WITH_TIMEZONE), liveOnly));
		}
		else
		// if we've found a non-empty collection splittable into parts other
		// than 3, we're heading for a null-pointer unless we head it off with
		// an explicit exception message
		{
			String splitMesg = "Expected to be able to split non-empty 'collection' string into 3 (if not 1) substring(s) seperated by ':', but found "
				+ length + " from 'collection' value: " + collection;
			throw new RuntimeException(splitMesg);
		}

		for( PropBagEx resultXml : resultsXml.iterateAll("result/xml/item") )
		{
			updated.add(convertToLO(resultXml));
		}

		return updated;
	}

	private EQUELLALearningObject convertToLO(PropBagEx resultXml) throws ParseException
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

		String id = resultXml.getNode("@id");
		String title = resultXml.getNode("name");

		String created = resultXml.getNode("datecreated");
		String modified = resultXml.getNode("datemodified");

		Date createdDate = dateFormat.parse(created);
		Date modifiedDate = dateFormat.parse(modified);

		boolean attachment = false;
		int nodeCount = resultXml.nodeCount("attachments/attachment");
		if( nodeCount > 0 )
		{
			attachment = true;
		}

		EQUELLALearningObject lObject = new EQUELLALearningObject(id, title, createdDate, attachment);
		lObject.setXml(resultXml);
		lObject.setModifiedDate(modifiedDate);
		return lObject;
	}

	@Override
	public String createAttachmentName(LearningObject lobject)
	{
		return null;
	}

	@Override
	public void downloadLO(LearningObject lobject, String stagingID) throws Exception
	{
		// Keep our local Sonar happy
		if( !(lobject instanceof EQUELLALearningObject) )
		{
			throw new ClassCastException("Learning Object must be EQUELLALearningObject");
		}
		PropBagEx resultXml = ((EQUELLALearningObject) lobject).getResultXml();

		int version = Integer.parseInt(resultXml.getNode("@version"));
		String uuid = resultXml.getNode("@id");

		final URL downloadUrl = new URL(soapService.prepareDownload(uuid, version));
		// unzipFile closes the InputStream, so we can forget about it.
		fileSystemService.unzipFile(new StagingFile(stagingID), downloadUrl.openStream(), ArchiveType.TAR_GZ);
	}

	@Override
	public Search getTLESearchRequest(LearningObject lobject)
	{
		return null;
	}

	@Override
	public void postProcessing(PropBagEx xml, LearningObject lobject) throws Exception
	{
		PropBagEx oldXml = ((EQUELLALearningObject) lobject).getResultXml(); // NOSONAR

		PropBagEx harvestXml = new PropBagEx(soapService.getItemXml(oldXml.getNode("@id"),
			Integer.parseInt(oldXml.getNode("@version"))));

		PropBagEx newXml = (PropBagEx) harvestXml.clone();

		applyTransform(newXml);

		newXml.setNode("item/@id", xml.getNode("item/@id"));
		newXml.setNode("item/@itemdefid", xml.getNode("item/@itemdefid"));
		newXml.setNode("item/@itemstatus", xml.getNode("item/@itemstatus"));
		newXml.setNode("item/@key", xml.getNode("item/@key"));
		newXml.setNode("item/@version", xml.getNode("item/@version"));
		newXml.setNode("item/staging", xml.getNode("item/staging"));
		newXml.setNode("item/newitem", xml.getNode("item/newitem"));
		newXml.setNode("item/owner", xml.getNode("item/owner"));

		xml.setXML(newXml.toString());

		((EQUELLALearningObject) lobject).setXml(xml);

	}

	private void applyTransform(PropBagEx xml) throws Exception
	{
		String xsltName = getProfile().getAttribute("schemaInputTransform");
		if( xsltName != null && !xsltName.isEmpty() )
		{
			LOGGR.info(CurrentLocale.get(KEY_PFX + "log.transform.before"));
			LOGGR.info(xml.toString());

			xml.setXML(transformSchema(xml, xsltName));

			LOGGR.info(CurrentLocale.get(KEY_PFX + "log.transform.after"));
			LOGGR.info(xml.toString());
		}
	}

	public static final class EQUELLALearningObject extends LearningObject
	{

		private PropBagEx resultXml;

		public EQUELLALearningObject(String identifier, String title, Date created, boolean attachment)
		{
			super(identifier, title, created, attachment);
			this.equellaItem = true;
		}

		public void setXml(PropBagEx resultXml)
		{
			this.setResultXml(resultXml);

		}

		public void setResultXml(PropBagEx resultXml)
		{
			this.resultXml = resultXml;
		}

		public PropBagEx getResultXml()
		{
			return resultXml;
		}
	}
}
