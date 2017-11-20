/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.api.provisioning;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.inject.Singleton;
import com.tle.core.events.GroupIdChangedEvent;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.events.services.EventService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Bind
@Singleton
@SuppressWarnings("nls")
@Path("group/{id}/action/")
@Produces({"application/json"})
@Api(value = "/group/{id}/action", description = "group-action")
public class GroupActionResource
{
	@Inject
	private EventService eventService;
	@Inject
	private TLEAclManager tleAclManager;

	@POST
	@Path("/changeid/{newid}")
	@ApiOperation(value = "Change all references from an existing group ID to a new group ID")
	public Response searchItems(
		// @formatter:off
		@ApiParam("Existing group ID") @PathParam("id") String groupId,
		@ApiParam("The new ID that all group references will be changed to") @PathParam("newid") String newGroupId
		// @formatter:on
	)
	{
		if( tleAclManager.filterNonGrantedPrivileges("EDIT_USER_MANAGEMENT").isEmpty() )
		{
			return Response.status(Status.UNAUTHORIZED).build();
		}

		eventService.publishApplicationEvent(new GroupIdChangedEvent(groupId, newGroupId));

		return Response.ok().build();
	}
}
