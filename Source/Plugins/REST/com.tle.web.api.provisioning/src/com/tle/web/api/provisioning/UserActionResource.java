package com.tle.web.api.provisioning;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.inject.Singleton;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.EventService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Bind
@Singleton
@SuppressWarnings("nls")
@Path("user/{id}/action/")
@Produces({"application/json"})
@Api(value = "/user/{id}/action", description = "user-action")
public class UserActionResource
{
	@Inject
	private EventService eventService;
	@Inject
	private TLEAclManager tleAclManager;

	@POST
	@Path("changeid/{newid}")
	@ApiOperation(value = "Change all references from an existing user ID to a new user ID")
	public Response changeAllUserReferences(
		// @formatter:off
		@ApiParam("Existing user ID") @PathParam("id") String userId,
		@ApiParam("The new ID that all user references will be changed to") @PathParam("newid") String newUserId
		// @formatter:on
	)
	{
		if( tleAclManager.filterNonGrantedPrivileges("EDIT_USER_MANAGEMENT").isEmpty() )
		{
			return Response.status(Status.UNAUTHORIZED).build();
		}

		eventService.publishApplicationEvent(new UserIdChangedEvent(userId, newUserId));

		return Response.ok().build();
	}
}
