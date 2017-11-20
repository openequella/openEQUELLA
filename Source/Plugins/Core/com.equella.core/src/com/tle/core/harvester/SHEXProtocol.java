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

import org.apache.log4j.Logger;

import com.tle.common.harvester.SHEXHarvesterSettings;
import com.tle.core.guice.Bind;

/**
 * @author larry
 */
@Bind
@SuppressWarnings("nls")
public class SHEXProtocol extends AbstractTLFProtocol
{
	private static final Logger LOGGR = Logger.getLogger(SHEXProtocol.class);
	private static final String SHEX_NAMESPACE_END = "/services/lorax/2008/07";
	private static final String SHEX_WEB_SERVICE = SHEX_NAMESPACE_END + "/Lorax.svc";
	private static final String SHEX_QUERY_SYNTAX = SHEX_NAMESPACE_END + "/Definitions/QuerySyntax";
	private static final String SHEX_RETRIEVE_CONTENT_METHOD = "retrieveContent";
	private static final String SHEX_QUERY_CONTENT_METHOD = "listChangedContent";
	private static final String SHEX_CONTENT_TYPE = "application/soap+xml";
	private static final String SHEX_SOAP_ENVELOPE_TAG_VALUE = "http://www.w3.org/2003/05/soap-envelope";

	public SHEXProtocol()
	{
		super();
	}

	@Override
	protected String getServerURLString()
	{
		return SHEXHarvesterSettings.SHEX_SERVER_URL;
	}

	@Override
	public String getWebServiceString()
	{
		return SHEX_WEB_SERVICE;
	}

	@Override
	protected String getRequestNamespace()
	{
		return getServerURLString() + SHEX_NAMESPACE_END;
	}

	@Override
	protected String getQuerySyntax()
	{
		return SHEX_QUERY_SYNTAX;
	}

	@Override
	protected String getRetrieveContentMethod()
	{
		return SHEX_RETRIEVE_CONTENT_METHOD;
	}

	@Override
	protected String getQueryContentMethod()
	{
		return SHEX_QUERY_CONTENT_METHOD;
	}

	@Override
	protected String getContentType()
	{
		return SHEX_CONTENT_TYPE;
	}

	@Override
	protected String getEnvelopeTagValue()
	{
		return SHEX_SOAP_ENVELOPE_TAG_VALUE;
	}

	@Override
	protected Logger getLogger()
	{
		return LOGGR;
	}
}
