package com.tle.core.harvester;

import org.apache.log4j.Logger;

import com.tle.common.harvester.MEXHarvesterSettings;
import com.tle.core.guice.Bind;

@Bind
@SuppressWarnings("nls")
public class MEXProtocol extends AbstractTLFProtocol
{
	private static final Logger LOGGR = Logger.getLogger(MEXProtocol.class);
	private static final String MEX_NAMESPACE_END = "/services/lorax/2008/07";
	private static final String MEX_WEB_SERVICE = MEX_NAMESPACE_END + "/Lorax.svc";
	private static final String MEX_QUERY_SYNTAX = MEX_NAMESPACE_END + "/Definitions/QuerySyntax";

	/**
	 * NB: not mex
	 */
	private static final String MEX_NAMESPACE_PREFIX = "http://sharing.thelearningfederation.edu.au";

	private static final String MEX_RETRIEVE_CONTENT_METHOD = "retrieveContent";
	private static final String MEX_QUERY_CONTENT_METHOD = "listChangedContent";
	private static final String MEX_CONTENT_TYPE = "application/soap+xml";
	private static final String MEX_SOAP_ENVELOPE_TAG_VALUE = "http://www.w3.org/2003/05/soap-envelope";

	public MEXProtocol()
	{
		super();
	}

	@Override
	protected String getServerURLString()
	{
		return MEXHarvesterSettings.MEX_SERVER_URL;
	}

	@Override
	protected String getWebServiceString()
	{
		return MEX_WEB_SERVICE;
	}

	@Override
	protected String getRequestNamespace()
	{
		return MEX_NAMESPACE_PREFIX + MEX_NAMESPACE_END;
	}

	@Override
	protected String getQuerySyntax()
	{
		return MEX_QUERY_SYNTAX;
	}

	@Override
	protected String getRetrieveContentMethod()
	{
		return MEX_RETRIEVE_CONTENT_METHOD;
	}

	@Override
	protected String getQueryContentMethod()
	{
		return MEX_QUERY_CONTENT_METHOD;
	}

	@Override
	protected String getContentType()
	{
		return MEX_CONTENT_TYPE;
	}

	@Override
	protected String getEnvelopeTagValue()
	{
		return MEX_SOAP_ENVELOPE_TAG_VALUE;
	}

	@Override
	protected Logger getLogger()
	{
		return LOGGR;
	}
}
