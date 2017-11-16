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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Produces({"application/json"})
@Path("item/{uuid}/{version}/lock")
@Api(value = "/item/{uuid}/{version}/lock", description = "item-lock")
public interface ItemLockResource
{
	@Path("")
	@GET
	@ApiOperation(value = "Get an existing lock for an item")
	public Response get(@Context UriInfo uriInfo, @PathParam("uuid") String uuid, @PathParam("version") int version);

	@Path("")
	@POST
	@ApiOperation(value = "Create a lock for an item")
	public Response lock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid, @PathParam("version") int version);

	@Path("")
	@DELETE
	@ApiOperation(value = "Unlock a locked item")
	public Response unlock(@PathParam("uuid") String uuid, @PathParam("version") int version);
}
