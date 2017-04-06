package com.tle.web.remoting.rest.docs;

import javax.servlet.ServletConfig;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.tle.core.guice.Bind;
import com.wordnik.swagger.annotations.Api;

@Bind
@Path("/resources")
@Api("/resources")
@Produces({MediaType.APPLICATION_JSON})
public class ApiListingResource extends com.wordnik.swagger.jaxrs.listing.ApiListingResource
{
	@Override
	public Response resourceListing(Application app, ServletConfig sc, HttpHeaders headers, UriInfo uriInfo)
	{
		Response resourceListing = super.resourceListing(app, sc, headers, uriInfo);
		return resourceListing;
	}
}
