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

package com.tle.web.api.users.interfaces;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.users.interfaces.beans.RoleBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

// Note: EQUELLA overrides the root Path
@Produces(MediaType.APPLICATION_JSON)
@Path("usermanagement/local/role/")
@Api(value = "/usermanagement/local/role", description = "usermanagement-local-role")
@SuppressWarnings("nls")
public interface RoleResource
{
	static final UriBuilder GETROLE = UriBuilder.fromResource(RoleResource.class).path(RoleResource.class, "getRole");
	static final String ACTIVITY_OBJECT_ROLE = "role";

	@GET
	@Path("/{uuid}")
	@ApiOperation("Retrieve a role")
	public RoleBean getRole(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@PUT
	@Path("/{uuid}")
	@ApiOperation("Edit a role")
	public Response editRole(@PathParam("uuid") String uuid, @ApiParam RoleBean role);

	@DELETE
	@Path("/{uuid}")
	@ApiOperation("Delete a role")
	public Response deleteRole(@PathParam("uuid") String uuid);

	@POST
	@Path("/")
	@ApiOperation("Add a role")
	public Response addRole(@ApiParam RoleBean role);

	@GET
	@Path("/name/{name}")
	@ApiOperation("Retrieve a role by name")
	public RoleBean getRoleByName(@Context UriInfo uriInfo, @PathParam("name") String name);

	@GET
	@Path("/")
	@ApiOperation("List internal roles")
	public SearchBean<RoleBean> list(@Context UriInfo uriInfo, @ApiParam(required = false) @QueryParam("q") String query);
}
