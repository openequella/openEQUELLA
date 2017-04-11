package com.tle.web.api.collection.interfaces;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.tle.web.api.collection.interfaces.beans.AllCollectionsSecurityBean;
import com.tle.web.api.collection.interfaces.beans.CollectionBean;
import com.tle.web.api.interfaces.BaseEntityResource;
import com.tle.web.api.interfaces.beans.EntityLockBean;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Produces(MediaType.APPLICATION_JSON)
@Path("collection/")
@Api(value = "/collection", description = "collection")
public interface CollectionResource extends BaseEntityResource<CollectionBean, AllCollectionsSecurityBean>
{
	@Override
	@GET
	@Path("/")
	@ApiOperation(value = "List all collections", notes = "Retrieve a list of all collections")
	public SearchBean<CollectionBean> list(@Context UriInfo uriInfo);

	@Override
	@GET
	@Path("/acl")
	@ApiOperation(value = "List global collection acls", notes = "Manage global collection ACLs")
	public AllCollectionsSecurityBean getAcls(@Context UriInfo uriInfo);

	@Override
	@PUT
	@Path("/acl")
	@ApiOperation(value = "Edit global collection acls")
	public Response editAcls(@Context UriInfo uriInfo,
		@ApiParam(value = "The global collection acls") AllCollectionsSecurityBean security);

	@Override
	@GET
	@Path("/{uuid}")
	@ApiOperation(value = "Get a collection", notes = "Manage collections")
	public CollectionBean get(@Context UriInfo uriInfo,
		@ApiParam(value = "Collection UUID") @PathParam("uuid") String uuid);

	@Override
	@DELETE
	@Path("/{uuid}")
	@ApiOperation("Delete a collection")
	public Response delete(@Context UriInfo uriInfo, @ApiParam(value = "Collection UUID") @PathParam("uuid") String uuid);

	@Override
	@POST
	@ApiOperation(value = "Create a new collection", notes = "Create new or list existing collections")
	@ApiResponses({@ApiResponse(code = 201, message = "Location: {newcollection uri}")})
	public Response create(@Context UriInfo uriInfo, @ApiParam(value = "Collection") CollectionBean bean,
		@ApiParam(required = false) @QueryParam(value = "file") String stagingUuid);

	@Override
	@PUT
	@Path("/{uuid}")
	@ApiOperation("Edit a collection")
	@ApiResponses({@ApiResponse(code = 200, message = "Location: {collection uri}")})
	public Response edit(@Context UriInfo uriInfo, @ApiParam(value = "Collection UUID") @PathParam("uuid") String uuid,
		@ApiParam CollectionBean bean,
		@ApiParam(required = false, value = "Staging area UUID") @QueryParam("file") String stagingUuid,
		@ApiParam(required = false, value = "The lock UUID if locked") @QueryParam("lock") String lockId,
		@ApiParam(value = "Unlock collection after edit") @QueryParam("keeplocked") boolean keepLocked);

	@Override
	@GET
	@Path("/{uuid}/lock")
	@ApiOperation(value = "Read the lock for a collection", response = EntityLockBean.class)
	public Response getLock(@Context UriInfo uriInfo,
		@ApiParam(value = "Collection UUID") @PathParam("uuid") String uuid);

	@Override
	@POST
	@Path("/{uuid}/lock")
	@ApiOperation(value = "Lock a collection", notes = "A collection lock will prevent others from editing it while locked", response = EntityLockBean.class)
	public Response lock(@Context UriInfo uriInfo, @ApiParam(value = "Collection UUID") @PathParam("uuid") String uuid);

	@Override
	@DELETE
	@Path("/{uuid}/lock")
	@ApiOperation("Unlock a collection")
	public Response unlock(@Context UriInfo uriInfo, @ApiParam(value = "Collection UUID") @PathParam("uuid") String uuid);
}
