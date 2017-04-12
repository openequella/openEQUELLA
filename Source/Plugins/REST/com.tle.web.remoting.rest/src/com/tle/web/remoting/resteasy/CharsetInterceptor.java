package com.tle.web.remoting.resteasy;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.ResourceListing;

@SuppressWarnings("nls")
@Provider
@ServerInterceptor
public class CharsetInterceptor implements PostProcessInterceptor
{
	private static final String CONTENT_TYPE_HEADER = "Content-Type";

	@Override
	public void postProcess(ServerResponse response)
	{
		final MultivaluedMap<String, Object> metadata = response.getMetadata();
		final Object contentType = metadata.getFirst(CONTENT_TYPE_HEADER);
		if( contentType != null )
		{
			final String ct;
			if( contentType instanceof MediaType )
			{
				MediaType mct = (MediaType) contentType;
				ct = mct.toString().toLowerCase();
			}
			else
			{
				ct = ((String) contentType).toLowerCase();
			}

			boolean isNotSwaggerResponse = (response.getGenericType() == null)
				|| (!response.getGenericType().equals(ResourceListing.class) && !response.getGenericType().equals(
					ApiListing.class));
			if( ct.contains("json") && !ct.contains("charset") && isNotSwaggerResponse )
			{
				metadata.putSingle(CONTENT_TYPE_HEADER, ct + "; charset=utf-8");
			}
		}
	}
}