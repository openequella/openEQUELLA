package com.tle.web.api.collection.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.tle.web.api.collection.interfaces.CollectionResource;
import com.tle.web.api.collection.interfaces.beans.CollectionBean;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author Aaron
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("collection/")
@Api(value = "/collection", description = "collection")
public interface EquellaCollectionResource extends CollectionResource
{
	@GET
	@Path("/")
	@ApiOperation(value = "List all collections", notes = "Retrieve a list of all collections")
	SearchBean<CollectionBean> list(@Context UriInfo uriInfo,
		@ApiParam(value = "privilege filter", required = false) @QueryParam("privilege") String privilege);
}
