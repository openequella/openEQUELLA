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

package com.tle.web.api.item.interfaces;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Produces({"application/json"})
@Path("item/{uuid}/{version}/action/")
@Api(value = "/item/{uuid}/{version}/action", description = "item-action")
@SuppressWarnings("nls")
public interface ItemActionsResource
{
	static final String APIDOC_UUID = "The uuid of the item to perform that action on";
	static final String APIDOC_VERSION = "The version of the item to perform that action on";

	@POST
	@Path("/submit")
	@ApiOperation(value = "Submit an item for moderation")
	public Response submit(// @formatter:off
		@ApiParam(APIDOC_UUID)
		@PathParam("uuid")
			String uuid,
		@ApiParam(APIDOC_VERSION)
		@PathParam("version")
			int version,
		@ApiParam("An optional submit message")
		@QueryParam("message") final
			String submitMessage);
	// @formatter:on

	@POST
	@Path("/redraft")
	@ApiOperation(value = "Redraft an item")
	public Response redraft(// @formatter:off
		@ApiParam(APIDOC_UUID)
		@PathParam("uuid")
			String uuid,
		@ApiParam(APIDOC_VERSION)
		@PathParam("version")
			int version);
	// @formatter:on

	@POST
	@Path("/archive")
	@ApiOperation(value = "Archive an item")
	public Response archive(// @formatter:off
		@ApiParam(APIDOC_UUID)
		@PathParam("uuid")
			String uuid,
		@ApiParam(APIDOC_VERSION)
		@PathParam("version")
			int version);
	// @formatter:on

	@POST
	@Path("/suspend")
	@ApiOperation(value = "Suspend an item")
	public Response suspend(// @formatter:off
		@ApiParam(APIDOC_UUID)
		@PathParam("uuid")
			String uuid,
		@ApiParam(APIDOC_VERSION)
		@PathParam("version")
			int version);
	// @formatter:on)

	@POST
	@Path("/reset")
	@ApiOperation(value = "Reset the workflow of an item")
	public Response reset(// @formatter:off
		@ApiParam(APIDOC_UUID)
		@PathParam("uuid")
			String uuid,
		@ApiParam(APIDOC_VERSION)
		@PathParam("version")
			int version);
	// @formatter:on)

	@POST
	@Path("/reactivate")
	@ApiOperation(value = "Make an item live again")
	public Response reactivate(// @formatter:off
		@ApiParam(APIDOC_UUID)
		@PathParam("uuid")
			String uuid,
		@ApiParam(APIDOC_VERSION)
		@PathParam("version")
			int version);
	// @formatter:on)

	@POST
	@Path("/restore")
	@ApiOperation(value = "Restore a deleted item")
	public Response restore(// @formatter:off
		@ApiParam(APIDOC_UUID)
		@PathParam("uuid")
			String uuid,
		@ApiParam(APIDOC_VERSION)
		@PathParam("version")
			int version);
	// @formatter:on)

	@POST
	@Path("/resume")
	@ApiOperation(value = "Resume a suspended item")
	public Response resume(// @formatter:off
		@ApiParam(APIDOC_UUID)
		@PathParam("uuid")
			String uuid,
		@ApiParam(APIDOC_VERSION)
		@PathParam("version")
			int version);
	// @formatter:on)

	@POST
	@Path("/review")
	@ApiOperation(value = "Review an item")
	public Response review(// @formatter:off
		@ApiParam(APIDOC_UUID)
		@PathParam("uuid")
			String uuid,
		@ApiParam(APIDOC_VERSION)
		@PathParam("version")
			int version);
	// @formatter:on)
}
