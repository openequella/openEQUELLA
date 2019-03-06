package com.tle.web.api.item.interfaces;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Produces({"application/json"})
@Path("item/{uuid}/{version}/lock")
@Api(value = "/item/{uuid}/{version}/lock", description = "item-lock")
public interface ItemLockResource
{
	@Path("")
	@GET
	@ApiOperation(value = "Get an existing lock for an item")
	public Response get(@Context UriInfo uriInfo, @PathParam("uuid") String uuid, @PathParam("version") int version);

	@Path("")
	@POST
	@ApiOperation(value = "Create a lock for an item")
	public Response lock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid, @PathParam("version") int version);

	@Path("")
	@DELETE
	@ApiOperation(value = "Unlock a locked item")
	public Response unlock(@PathParam("uuid") String uuid, @PathParam("version") int version);
}
