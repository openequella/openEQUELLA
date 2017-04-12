package com.tle.web.api.workflow;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.tle.web.api.item.tasks.interfaces.TaskResource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author Aaron
 */
@Path("task")
@Api(value = "/task", description = "task")
@Produces(MediaType.APPLICATION_JSON)
public interface EquellaTaskResource extends TaskResource
{
	@GET
	@Path("/filter")
	@ApiOperation(value = "Get the counts for each task type")
	public Response getTaskFilters(
		// @formatter:off
		@ApiParam(value = "Do not return task filters that contain no tasks. Implies includeCounts=true.", allowableValues = "true,false", defaultValue = "false", required = false)
		@QueryParam("ignoreZero")
			boolean ignoreZeroStr,
		@ApiParam(value = "Include task counts against each filter name.", allowableValues = "true,false", defaultValue = "false", required = false)
		@QueryParam("includeCounts")
			boolean includeCounts
		);
		// @formatter:on
}
