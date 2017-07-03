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
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;

import com.google.gdata.util.common.util.Base64;
import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.google.common.io.Closeables;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.common.searching.Search;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.harvester.old.LearningObject;
import com.tle.core.harvester.old.dsoap.RequestParameter;
import com.tle.core.harvester.old.dsoap.SoapCall;
import com.tle.core.harvester.old.dsoap.sax.ElementResultSoapHandler;
import com.tle.core.harvester.search.HarvesterSearch;
import com.tle.core.services.FileSystemService;

/**
 * With the addition of the SHEX and MEX harvesters which differ from the LORAX
 * harvester only in minor details, this class represents a common TLF Protocol
 * to house the common code. The source for this code is the LORAXProtocol
 * class. That class meanwhile is altered into a subclass of this class.
 * 
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class AbstractTLFProtocol extends AbstractHarvesterProtocol
{
	protected final DateFormat QUERY_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	protected final DateFormat VERSION_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private static final String RESULT_NODE = "contentChange";
	private static final String RESULT_IDENTIFIER_NODE = "identifier";
	private static final String RESULT_DATE_NODE = "changeDate";
	/**
	 * SOAP call may have a SOAPAction property, which is simply the method name
	 * suffixed, hence method name chaseHeadlessRooster, SOAPAction
	 * chaseHeaDlessRoosterRequest
	 */
	private final static String ACTION_SUFFIX = "Request";
	private final static String SOAP_ENVELOPE_TAG = "soap";

	@Inject
	private FileSystemService fileSystemService;

	private String username;
	private String password;
	private boolean harvestResources;
	private boolean harvestLearningObjects;
	private boolean onlyCheckForLiveVersions;

	protected abstract String getServerURLString();

	protected abstract String getWebServiceString();

	/**
	 * sometimes the same as the URL, sometimes not
	 * 
	 * @return
	 */
	protected abstract String getRequestNamespace();

	protected abstract String getQuerySyntax();

	protected abstract String getRetrieveContentMethod();

	protected abstract String getQueryContentMethod();

	protected abstract String getContentType();

	protected abstract String getEnvelopeTagValue();

	/**
	 * sublclasses may have another, or perhaps no suffix at all (empty string)
	 * 
	 * @return
	 */
	protected String getActionSuffix()
	{
		return ACTION_SUFFIX;
	}

	protected abstract Logger getLogger();

	@Override
	public int setupAndRun(HarvesterProfile profile, boolean testOnly) throws Exception
	{
		String url = getServerURLString();
		URL server = new URL(url);
		this.host = server.getHost();
		this.port = server.getPort();
		this.context = server.getPath() + getWebServiceString();

		username = profile.getAttribute("user");
		password = profile.getAttribute("pass");

		setNamespace(getRequestNamespace());

		// What should we harvest?
		harvestResources = profile.getAttribute("harvestResources", false);
		harvestLearningObjects = profile.getAttribute("harvestLearningObjects", true);
		onlyCheckForLiveVersions = profile.getAttribute("liveOnly", true);

		return super.setupAndRun(profile, testOnly);
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.lexharvester.repositories.ContentRepository#
	 * getTLESearchRequest(
	 * com.dytech.edge.lexharvester.repositories.LearningObject)
	 */
	@Override
	public Search getTLESearchRequest(LearningObject lobject)
	{
		HarvesterSearch search = new HarvesterSearch();

		FreeTextBooleanQuery ftquery = WhereParser.parse("WHERE /xml/item/itembody/tlfid = '" + lobject.getIdentifier()
			+ '\'');
		search.setFreeTextQuery(ftquery);
		if( onlyCheckForLiveVersions )
		{
			search.setItemStatuses(ItemStatus.LIVE, ItemStatus.REVIEW);
		}

		return search;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.edge.lexharvester.repositories.ContentRepository#
	 * createAttachmentName(
	 * com.dytech.edge.lexharvester.repositories.LearningObject)
	 */
	@Override
	public String createAttachmentName(LearningObject lobject)
	{
		String name = lobject.getTitle();

		StringBuilder buffer = new StringBuilder();

		final int count = name.length();
		for( int i = 0; i < count; i++ )
		{
			char c = name.charAt(i);
			if( Character.isLetterOrDigit(c) || Character.isWhitespace(c) )
			{
				buffer.append(c);
			}
		}

		String result = buffer.toString();
		result = result.trim();

		if( result.length() == 0 )
		{
			result = "Unspecified Name";
		}

		return result + ".zip";
	}

	protected PropBagEx generateQuery(Date since)
	{
		PropBagEx query = new PropBagEx();
		query = query.newSubtree("QUERY");
		query.setNode("@xmlns", "");

		PropBagEx and = query.newSubtree("AND");
		and.setNode("Title", "*");
		and.setNode("VersionDate", QUERY_DATE_FORMAT.format(since));
		and.setNode("VersionDate/@op", ">=");

		// LEX have two types of objects; Learning Objects and Resources. Most
		// installations are only going to want Learning Objects. The catalog
		// types are:
		//
		// - TLF-LearningObject
		// - TLF-Resource
		//
		// Not specifying a catalog type will get all of them.

		if( harvestResources && harvestLearningObjects )
		{
			getLogger().info(CurrentLocale.get("com.tle.core.harvester.log.lorax.both"));
		}
		else if( harvestLearningObjects )
		{
			getLogger().info(CurrentLocale.get("com.tle.core.harvester.log.lorax.learning"));
			and.setNode("Catalog", "TLF-LearningObject");
		}
		else if( harvestResources )
		{
			getLogger().info(CurrentLocale.get("com.tle.core.harvester.log.lorax.resources"));
			and.setNode("Catalog", "TLF-Resource");
		}

		return query;
	}

	/**
	 * Similiar to <code>constructCall</code>, but does some extra things that
	 * Lex needs.
	 */
	protected SoapCall myConstructCall(String method, String actionSuffix)
	{
		SoapCall call = constructCall(method);
		call.setSOAPAction(getNamespace() + '/' + method + actionSuffix);
		call.setEnvelopeTag(SOAP_ENVELOPE_TAG);
		// The default is populated; this is to overwrite if supplied
		if( !Check.isEmpty(getEnvelopeTagValue()) )
		{
			call.setEnvelopeTagValue(getEnvelopeTagValue());
		}
		call.setDotNetCompatability(true);
		return call;
	}

	/**
	 * Add the username and password parameters to the given soap call.
	 */
	protected SoapCall addCredentials(SoapCall call, boolean lowercase)
	{
		String un = "Username";
		String pw = "Password";
		if( lowercase )
		{
			un = un.toLowerCase();
			pw = pw.toLowerCase();
		}
		addParameter(call, un, username);
		addParameter(call, pw, password);
		return call;
	}

	private String host;
	private int port;
	private String context;
	private String namespace;

	protected final void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}

	protected final String getNamespace()
	{
		return namespace;
	}

	/**
	 * Helper method to construct a SOAP call.
	 * 
	 * @param methodName Name of the method being called.
	 * @return The new SoapCall object.
	 */
	protected final SoapCall constructCall(String methodName)
	{
		if( namespace == null )
		{
			return new SoapCall(host, port, context, methodName);
		}
		else
		{
			return new SoapCall(host, port, context, methodName, namespace);
		}
	}

	/**
	 * Helper method to add a new String parameter to a SOAP call.
	 * 
	 * @param call The SoapCall to add the parameter to.
	 * @param name The name of the parameter.
	 * @param value The value of the parameter.
	 * @throws SoapCallException only if something goes terribly wrong.
	 */
	protected final void addParameter(SoapCall call, String name, String value)
	{
		call.addParameter(new RequestParameter(name, value));
	}

	/**
	 * Helper method to add a new String parameter to a SOAP call.
	 * 
	 * @param call The SoapCall to add the parameter to.
	 * @param name The name of the parameter.
	 * @param value The value of the parameter.
	 * @throws SoapCallException only if something goes terribly wrong.
	 */
	protected final void addParameter(SoapCall call, String name, int value)
	{
		call.addParameter(new RequestParameter(name, value));
	}

	/**
	 * Helper method to add a new String parameter to a SOAP call.
	 * 
	 * @param call The SoapCall to add the parameter to.
	 * @param name The name of the parameter.
	 * @param value The value of the parameter.
	 * @throws SoapCallException only if something goes terribly wrong.
	 */
	protected final void addParameter(SoapCall call, String name, String[] values)
	{
		call.addParameter(new RequestParameter(name, values));
	}

	/**
	 * Helper method to add a new boolean parameter to a SOAP call.
	 * 
	 * @param call The SoapCall to add the parameter to.
	 * @param name The name of the parameter.
	 * @param value The value of the parameter.
	 * @throws SoapCallException only if something goes terribly wrong.
	 */
	protected final void addParameter(SoapCall call, String name, boolean value)
	{
		call.addParameter(new RequestParameter(name, value));
	}

	/**
	 * Helper method to add a new XML parameter to a SOAP call.
	 * 
	 * @param call The SoapCall to add the parameter to.
	 * @param name The name of the parameter.
	 * @param value The value of the parameter.
	 * @throws SoapCallException only if something goes terribly wrong.
	 */
	protected final void addParameter(SoapCall call, String name, PropBagEx value, String def)
	{
		call.addParameter(new RequestParameter(name, value.toString(), RequestParameter.RP_COMPLEX, def));
	}

	@Override
	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.edge.lexharvester.repositories.ContentRepository#postProcessing
	 * ( com.dytech.devlib.PropBagEx,
	 * com.dytech.edge.lexharvester.repositories.LearningObject)
	 */
	public void postProcessing(PropBagEx xml, LearningObject lobject)
	{
		PropBagEx itembody = xml.aquireSubtree("item/itembody");
		itembody.setNode("packagefile", createAttachmentName(lobject));
		itembody.setNode("tlfid", lobject.getIdentifier());
		itembody.setNode("type", "IMS Package");
	}

	/**
	 * Common to SHEX & MEX, overriden by LORAX
	 */
	@Override
	public Collection<LearningObject> getUpdatedLearningObjects(Date since) throws Exception
	{
		SoapCall call = myConstructCall(getQueryContentMethod(), getActionSuffix());
		addCredentials(call, true);

		addParameter(call, "changedSince", VERSION_DATE_FORMAT.format(since));

		ElementResultSoapHandler handler = new ElementResultSoapHandler(2);
		call.callWithSoapSAX(handler, getContentType());
		PropBagEx queryXml = new PropBagEx(handler.getElementResult());

		PropBagEx resultRootNode = queryXml.getSubtree(getQueryContentMethod() + "Result");

		Collection<LearningObject> results = new ArrayList<LearningObject>();

		if( resultRootNode != null )
		{
			for( PropBagEx result : resultRootNode.iterator(RESULT_NODE) )
			{
				String identifier = result.getNode(RESULT_IDENTIFIER_NODE);
				String created = result.getNode(RESULT_DATE_NODE);

				// In theory, a contentChange node can be empty
				if( !Check.isEmpty(identifier) || !Check.isEmpty(created) )
				{
					Date creationDate = null;
					try
					{
						creationDate = VERSION_DATE_FORMAT.parse(created);
					}
					catch( ParseException ex )
					{
						getLogger().error(
							CurrentLocale.get("com.tle.core.harvester.learning.badformat", RESULT_DATE_NODE, created));
						throw ex;
					}

					results.add(new LearningObject(identifier, identifier, creationDate, true));
				}
			}
		}
		return results;
	}

	/**
	 * Common to SHEX & MEX, overriden by LORAX
	 */
	@Override
	public void downloadLO(LearningObject lobject, String stagingID) throws Exception
	{
		PropBagEx responseXml = null;
		OutputStream out = null;

		StagingFile staging = new StagingFile(stagingID);
		String filename = createAttachmentName(lobject);
		try
		{
			out = getOutputStream(staging, filename);
			SoapCall call = myConstructCall(getRetrieveContentMethod(), getActionSuffix());
			addCredentials(call, true);
			addParameter(call, "identifier", lobject.getIdentifier());
			ElementResultSoapHandler handler = new ElementResultSoapHandler(2);
			call.callWithSoapSAX(handler, getContentType());
			responseXml = new PropBagEx(handler.getElementResult());
			String base64Data = responseXml.getNode("retrieveContentResult");
			out.write(Base64.decode(base64Data));
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
