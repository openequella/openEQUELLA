package com.tle.web.remoting.rest.docs;

import javax.ws.rs.core.MultivaluedMap;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.util.HttpResponseCodes;

public class DocsRedirect extends ServerResponse
{
	private Headers<Object> headers = new Headers<Object>();

	public DocsRedirect(String url)
	{
		super();
		headers.add("Location", url); //$NON-NLS-1$ 
	}

	@Override
	public Object getEntity()
	{
		return null;
	}

	@Override
	public int getStatus()
	{
		return HttpResponseCodes.SC_MOVED_TEMPORARILY;
	}

	@Override
	public MultivaluedMap<String, Object> getMetadata()
	{
		return headers;
	}

}
