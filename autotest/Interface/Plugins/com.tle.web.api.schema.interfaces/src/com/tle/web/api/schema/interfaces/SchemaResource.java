package com.tle.web.api.schema.interfaces;

import com.tle.web.api.interfaces.BaseEntityResource;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.schema.interfaces.beans.SchemaBean;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Produces({"application/json"})
@Path("schema/")
@Api(value = "/schema", description = "schema")
public interface SchemaResource extends BaseEntityResource<SchemaBean, BaseEntitySecurityBean> {
  @Override
  @GET
  @Path("/acl")
  @ApiOperation(value = "List global schema acls")
  public BaseEntitySecurityBean getAcls(@Context UriInfo uriInfo);

  @Override
  @PUT
  @Path("/acl")
  @ApiOperation(value = "Edit global schema acls")
  public Response editAcls(@Context UriInfo uriInfo, BaseEntitySecurityBean security);

  @Override
  @GET
  @ApiOperation(value = "List all schemas")
  public SearchBean<SchemaBean> list(@Context UriInfo uriInfo);

  @Override
  @GET
  @Path("/{uuid}")
  @ApiOperation(value = "Get a schema")
  public SchemaBean get(
      @Context UriInfo uriInfo, @ApiParam("schema uuid") @PathParam("uuid") String uuid);

  @Override
  @DELETE
  @Path("/{uuid}")
  @ApiOperation("Delete a schema")
  public Response delete(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

  @Override
  @POST
  @ApiOperation("Create a new schema")
  public Response create(
      @Context UriInfo uriInfo,
      @ApiParam SchemaBean bean,
      @ApiParam(required = false) @QueryParam("file") String stagingUuid);

  @Override
  @PUT
  @Path("/{uuid}")
  @ApiOperation(value = "Edit a schema")
  public Response edit(
      @Context UriInfo uriInfo,
      @PathParam("uuid") String uuid,
      @ApiParam SchemaBean bean,
      @ApiParam(required = false) @QueryParam("file") String stagingUuid,
      @ApiParam(required = false) @QueryParam("lock") String lockId,
      @ApiParam(required = false) @QueryParam("keeplocked") boolean keepLocked);

  @Override
  @GET
  @Path("/{uuid}/lock")
  @ApiOperation("Read the lock for a schema")
  public Response getLock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

  @Override
  @POST
  @Path("/{uuid}/lock")
  @ApiOperation("Lock a schema")
  public Response lock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

  @Override
  @DELETE
  @Path("/{uuid}/lock")
  @ApiOperation("Unlock a schema")
  public Response unlock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);
}
