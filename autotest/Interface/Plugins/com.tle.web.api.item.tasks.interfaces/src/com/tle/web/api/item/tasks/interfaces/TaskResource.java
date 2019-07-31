package com.tle.web.api.item.tasks.interfaces;

import com.tle.common.interfaces.CsvList;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("task")
@Api(value = "/task", description = "task")
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("nls")
public interface TaskResource {
  static final String DEFAULT_FILTER = "all";

  @GET
  @Path("/")
  @ApiOperation(value = "Search tasks")
  public Response tasksSearch(
      // @formatter:off
      @Context UriInfo uriInfo,
      @ApiParam(
              value = "The filtering to apply to the task list",
              allowableValues = "all,assignedme,assignedothers,assignednone,mustmoderate",
              required = false,
              defaultValue = DEFAULT_FILTER)
          @QueryParam("filter")
          String filtering,
      @ApiParam(value = "Query string", required = false) @QueryParam("q") String q,
      @ApiParam(
              value = "The first record of the search results to return",
              required = false,
              defaultValue = "0")
          @QueryParam("start")
          int start,
      @ApiParam(
              value = "The number of results to return",
              required = false,
              defaultValue = "10",
              allowableValues = "range[1,100]")
          @QueryParam("length")
          int length,
      @ApiParam(value = "List of collections", required = false) @QueryParam("collections")
          CsvList collections,
      @ApiParam(
              value = "The order of the search results",
              allowableValues = "priority,duedate,waiting, name",
              required = false)
          @QueryParam("order")
          String order,
      @ApiParam(
              value = "Reverse the order of the search results",
              allowableValues = ",true,false",
              defaultValue = "false",
              required = false)
          @QueryParam("reverse")
          String reverse);
  // @formatter:on
}
