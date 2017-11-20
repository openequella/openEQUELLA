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

package com.tle.web.api.item.tasks.interfaces;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Produces({"application/json"})
@Path("item/{uuid}/{version}/task/{taskUuid}/")
@Api(value = "/item/{uuid}/{version}/task/{taskUuid}", description = "item-task")
public interface ItemTaskResource
{
	@POST
	@Path("/accept")
	@ApiOperation("Accept a task on an item")
	public Response accept(@PathParam("uuid") final String uuid, @PathParam("version") final int version,
		@PathParam("taskUuid") final String taskUuid, @QueryParam("message") final String message);

	@POST
	@Path("/reject")
	@ApiOperation("Reject a task on an item")
	public Response reject(@PathParam("uuid") final String uuid, @PathParam("version") final int version,
		@PathParam("taskUuid") final String taskUuid, @QueryParam("message") final String message,
		@QueryParam("to") final String nodeUuid);

	@POST
	@Path("/comment")
	@ApiOperation("Comment on a task for an item")
	public Response comment(@PathParam("uuid") final String uuid, @PathParam("version") final int version,
		@PathParam("taskUuid") final String taskUuid, @QueryParam("message") final String message);
}
