package com.tle.web.sections;

import hurl.build.UriBuilder;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

public class StandardPathGenerator implements PathGenerator
{

	@Override
	public URI getBaseHref(SectionInfo info)
	{
		URI baseHref = info.getAttribute(SectionInfo.KEY_BASE_HREF);
		if( baseHref == null )
		{
			baseHref = createFromRequest(info.getRequest());
		}
		return baseHref;
	}

	private URI createFromRequest(HttpServletRequest request)
	{
		UriBuilder uriBuilder = UriBuilder.create(request.getRequestURI());
		uriBuilder.setScheme(request.getScheme());
		uriBuilder.setHost(request.getServerName());
		uriBuilder.setPort(request.getServerPort());
		return uriBuilder.build();
	}

	@Override
	public URI getRelativeURI(SectionInfo info)
	{
		String path = info.getAttribute(SectionInfo.KEY_PATH);
		try
		{
			return new URI(null, null, path.substring(1), null);
		}
		catch( URISyntaxException e )
		{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public URI getFullURI(SectionInfo info)
	{
		return getBaseHref(info).resolve(getRelativeURI(info));
	}

}
