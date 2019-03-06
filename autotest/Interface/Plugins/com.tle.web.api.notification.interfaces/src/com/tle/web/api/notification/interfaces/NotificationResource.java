package com.tle.web.api.notification.interfaces;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.tle.common.interfaces.CsvList;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("notification")
@Api(value = "/notification", description = "notification")
@Produces({"application/json"})
public interface NotificationResource
{
	@GET
	@Path("/")
	@ApiOperation(value = "Search notifications")
	public Response notificationsSearch(
		@Context UriInfo uriInfo,
		@ApiParam(value = "The filter to apply to the notifications list", required = false, allowableValues = "all,wentlive,rejected,badurl,watchedwentlive,overdue", defaultValue = "all") @QueryParam("type") String searchFilter,
		@ApiParam(value = "Query string", required = false) @QueryParam("q") String q,
		@ApiParam(value = "The first record of the search results to return", required = false, defaultValue = "0") @QueryParam("start") int start,
		@ApiParam(value = "The number of results to return", required = false, defaultValue = "10", allowableValues = "range[1,100]") @QueryParam("length") int length,
		@ApiParam(value = "List of collections", required = false) @QueryParam("collections") CsvList collections);

	@DELETE
	@Path("/{notificationId}")
	@ApiOperation(value = "Clear notifications")
	public Response delete(
		@ApiParam("Notification to clear") @PathParam("notificationId") String notificationId,
		@ApiParam(value = "Whether or not to wait for the item to be indexed before returning", defaultValue = "false") @QueryParam("waitforindex") boolean waitForIndex);
}