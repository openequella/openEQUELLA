package com.tle.web.remoting.rest.service;

import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;

/**
 * 
 */
@Bind(UrlLinkService.class)
public class UrlLinkServiceImpl implements UrlLinkService
{
	@Inject
	private UrlService urlService;

	@Override
	public UriBuilder getMethodUriBuilder(Class<?> resource, String method)
	{
		try
		{
			UriBuilder builder = UriBuilder.fromUri(urlService.getInstitutionUrl().toURI());
			builder.path("api");
			return builder.path(resource).path(resource, method);
		}
		catch( URISyntaxException e )
		{
			throw new RuntimeException(e);
		}
	}
}
