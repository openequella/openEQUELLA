package com.tle.web.api.item.tasks.interfaces;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Produces({"application/json"})
@Path("item/{uuid}/{version}/task/{taskUuid}/")
@Api(value = "/item/{uuid}/{version}/task/{taskUuid}", description = "item-task")
public interface ItemTaskResource {
  @POST
  @Path("/accept")
  @ApiOperation("Accept a task on an item")
  public Response accept(
      @PathParam("uuid") final String uuid,
      @PathParam("version") final int version,
      @PathParam("taskUuid") final String taskUuid,
      @QueryParam("message") final String message);

  @POST
  @Path("/reject")
  @ApiOperation("Reject a task on an item")
  public Response reject(
      @PathParam("uuid") final String uuid,
      @PathParam("version") final int version,
      @PathParam("taskUuid") final String taskUuid,
      @QueryParam("message") final String message,
      @QueryParam("to") final String nodeUuid);

  @POST
  @Path("/comment")
  @ApiOperation("Comment on a task for an item")
  public Response comment(
      @PathParam("uuid") final String uuid,
      @PathParam("version") final int version,
      @PathParam("taskUuid") final String taskUuid,
      @QueryParam("message") final String message);
}
