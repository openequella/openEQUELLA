package com.tle.web.api.workflow.interfaces;

import com.tle.web.api.interfaces.BaseEntityResource;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.workflow.interfaces.beans.WorkflowBean;
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
@Path("workflow/")
@Api(value = "/workflow", description = "workflow")
public interface WorkflowResource extends BaseEntityResource<WorkflowBean, BaseEntitySecurityBean> {
  @Override
  @GET
  @Path("/acl")
  @ApiOperation(value = "List global workflow acls")
  public BaseEntitySecurityBean getAcls(@Context UriInfo uriInfo);

  @Override
  @PUT
  @Path("/acl")
  @ApiOperation(value = "Edit global workflow acls")
  public Response editAcls(@Context UriInfo uriInfo, BaseEntitySecurityBean security);

  @Override
  @GET
  @ApiOperation("List all workflows")
  public SearchBean<WorkflowBean> list(@Context UriInfo uriInfo);

  @Override
  @GET
  @Path("/{uuid}")
  @ApiOperation("Get a workflow")
  public WorkflowBean get(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

  @Override
  @DELETE
  @Path("/{uuid}")
  @ApiOperation("Delete a workflow")
  public Response delete(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

  @Override
  @POST
  @ApiOperation("Create a new workflow")
  public Response create(
      @Context UriInfo uriInfo,
      @ApiParam WorkflowBean bean,
      @QueryParam("file") String stagingUuid);

  @Override
  @PUT
  @Path("/{uuid}")
  @ApiOperation("Edit a workflow")
  public Response edit(
      @Context UriInfo uriInfo,
      @ApiParam @PathParam("uuid") String uuid,
      @ApiParam WorkflowBean bean,
      @ApiParam(required = false) @QueryParam("file") String stagingUuid,
      @ApiParam(required = false) @QueryParam("lock") String lockId,
      @ApiParam(required = false) @QueryParam("keeplocked") boolean keepLocked);

  @Override
  @GET
  @Path("/{uuid}/lock")
  @ApiOperation("Read the lock for a workflow")
  public Response getLock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

  @Override
  @POST
  @Path("/{uuid}/lock")
  @ApiOperation("Lock a workflow")
  public Response lock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

  @Override
  @DELETE
  @Path("/{uuid}/lock")
  @ApiOperation("Unlock a workflow")
  public Response unlock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);
}
