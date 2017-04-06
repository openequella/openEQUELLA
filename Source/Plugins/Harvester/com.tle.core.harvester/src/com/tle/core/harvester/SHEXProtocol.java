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
