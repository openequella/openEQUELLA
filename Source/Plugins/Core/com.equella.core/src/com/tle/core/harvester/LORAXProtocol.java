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

import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.google.common.io.Closeables;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.harvester.LORAXHarvesterSettings;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.old.LearningObject;
import com.tle.core.harvester.old.TLFSaxDownloader;
import com.tle.core.harvester.old.dsoap.SoapCall;
import com.tle.core.harvester.old.dsoap.sax.ElementResultSoapHandler;
import com.tle.core.services.FileSystemService;

/**
 * @author larry
 */
@Bind
@SuppressWarnings("nls")
public class LORAXProtocol extends AbstractTLFProtocol
{
	private static final Logger LOGGR = Logger.getLogger(LORAXProtocol.class);
	private static final String LORAX_NAMESPACE_PATH = "/webservices/2006/12";
	private static final String LORAX_NAMESPACE_END = "/LORAX";
	private static final String LORAX_WEB_SERVICE = LORAX_NAMESPACE_PATH + "/LORAX.asmx";
	private static final String LORAX_QUERY_SYNTAX = LORAX_NAMESPACE_PATH + "/Definitions/QuerySyntax";
	private static final String LORAX_RETRIEVE_CONTENT_METHOD = "RetrieveContent";
	private static final String LORAX_QUERY_CONTENT_METHOD = "QueryContent";
	private static final String LORAX_CONTENT_TYPE = "text/xml";
	private static final String LORAX_SOAP_ENVELOPE_TAG_VALUE = "http://schemas.xmlsoap.org/soap/envelope/";

	@Inject
	private FileSystemService fileSystemService;

	@Override
	protected String getServerURLString()
	{
		return LORAXHarvesterSettings.LORAX_SERVER_URL;
	}

	@Override
	public String getWebServiceString()
	{
		return LORAX_WEB_SERVICE;
	}

	@Override
	protected String getRequestNamespace()
	{
		return getServerURLString() + LORAX_NAMESPACE_PATH + LORAX_NAMESPACE_END;
	}

	@Override
	protected String getQuerySyntax()
	{
		return LORAX_QUERY_SYNTAX;
	}

	@Override
	protected String getRetrieveContentMethod()
	{
		return LORAX_RETRIEVE_CONTENT_METHOD;
	}

	@Override
	protected String getQueryContentMethod()
	{
		return LORAX_QUERY_CONTENT_METHOD;
	}

	@Override
	protected String getContentType()
	{
		return LORAX_CONTENT_TYPE;
	}

	@Override
	protected String getActionSuffix()
	{
		return "";
	}

	@Override
	protected String getEnvelopeTagValue()
	{
		return LORAX_SOAP_ENVELOPE_TAG_VALUE;
	}

	@Override
	protected Logger getLogger()
	{
		return LOGGR;
	}

	/**
	 * Differs from SHEX & MEX, so overrides the the version they derive from
	 * AbstractTLFProtocol
	 */
	@Override
	public Collection<LearningObject> getUpdatedLearningObjects(Date since) throws Exception
	{
		PropBagEx query = generateQuery(since);

		SoapCall call = myConstructCall(getQueryContentMethod(), getActionSuffix());
		String querySyntax = getServerURLString() + getQuerySyntax();

		addParameter(call, "QUERY", query, querySyntax);

		addCredentials(call, true);

		ElementResultSoapHandler handler = new ElementResultSoapHandler(2);
		call.callWithSoapSAX(handler, getContentType());
		PropBagEx queryXml = new PropBagEx(handler.getElementResult());

		Collection<LearningObject> results = new ArrayList<LearningObject>();

		for( PropBagEx result : queryXml.iterator(getQueryContentMethod() + "Result") )
		{
			String identifier = result.getNode("Identifier");
			String title = result.getNode("Title");
			String created = result.getNode("VersionDateTime");

			Date creationDate = null;
			try
			{
				creationDate = VERSION_DATE_FORMAT.parse(created);
			}
			catch( ParseException ex )
			{
				getLogger().error(
					CurrentLocale.get("com.tle.core.harvester.learning.badformat", "VersionDateTime", created));
				throw ex;
			}

			results.add(new LearningObject(identifier, title, creationDate, true));
		}

		return results;
	}

	/**
	 * Differs from SHEX & MEX, so overrides the the version they derive from
	 * AbstractTLFProtocol
	 */
	@Override
	public void downloadLO(LearningObject lobject, String stagingID) throws Exception
	{
		OutputStream out = null;

		StagingFile staging = new StagingFile(stagingID);
		String filename = createAttachmentName(lobject);
		try
		{
			// if we are harvesting with a provision to edit-in-place, an
			// original may be a directory (unpacked zip for example) so it's
			// necessary to call for a removal action (because merely opening a
			// plain file for rewrite will fail on the grounds that can't
			// overwrite a file "Is a directory")
			boolean removed = fileSystemService.removeFile(staging, filename);
			if( removed )
			{
				LOGGR.info("removed " + staging + "/" + filename + " ahead of rewrite");
			}
			out = getOutputStream(staging, filename);
			SoapCall call = myConstructCall(getRetrieveContentMethod(), getActionSuffix());
			addParameter(call, "contentIdentifier", lobject.getIdentifier());
			addParameter(call, "packageFormat", "");
			addParameter(call, "rightsDigitalFormat", "DytechREL");
			addCredentials(call, true);
			call.callWithSAX(new TLFSaxDownloader(out), getContentType());
		}
		catch( Exception ex )
		{
			getLogger().error(
				CurrentLocale.get("com.tle.core.harvester.error.lorax.download", lobject.getIdentifier()), ex);
			throw ex;
		}
		finally
		{
			Closeables.close(out, true); // Quietly
		}
		fileSystemService.unzipFile(staging, filename, "_" + filename);
		fileSystemService.removeFile(staging, filename);
		fileSystemService.rename(staging, "_" + filename, filename);

	}
}
