package com.tle.web.remoting.rest.docs;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;

@Bind
@Produces({"text/html"})
@Path("/")
@SuppressWarnings("nls")
public class DocsResource
{
	@Inject
	private UrlService urlService;

	@GET
	@Path("/")
	@Produces("text/html")
	public Response docs()
	{
		return new DocsRedirect(urlService.institutionalise("apidocs.do"));
	}
}
