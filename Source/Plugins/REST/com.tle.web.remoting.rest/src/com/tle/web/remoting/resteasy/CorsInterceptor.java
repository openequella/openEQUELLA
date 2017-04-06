package com.tle.web.remoting.resteasy;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

@Provider
@ServerInterceptor
@SuppressWarnings("nls")
public class CorsInterceptor implements PostProcessInterceptor
{
	@Override
	public void postProcess(ServerResponse response)
	{
		process(response);
	}

	public static void runPostProcess(ServerResponse response)
	{
		process(response);
	}

	private static void process(ServerResponse response)
	{
		final MultivaluedMap<String, Object> metadata = response.getMetadata();
		metadata.putSingle("Access-Control-Allow-Origin", "*");
		metadata.putSingle("Access-Control-Expose-Headers", "Location");
	}
}