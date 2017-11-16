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

package com.tle.web.api.activation;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.tle.web.api.interfaces.beans.SearchBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Dongsheng Cai
 */
@Path("activation/")
@Api(value = "/activation", description = "activation")
@Produces({"application/json"})
public interface ActivationResource
{
	/**
	 * Retrieve all activations, or if ...?course={courseUuid} param appended,
	 * retrieve just those activations associated with the specified course.
	 *
	 * @param courseUuid
	 * @return
	 */
	@GET
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Retrieve activations")
	SearchBean<ActivationBean> search(
		// @formatter:off
			@ApiParam(value = "Course uuid", required = false) @QueryParam("course") String courseUuid,
			@ApiParam(value = "status", required = false, allowableValues = "active,pending,expired,any") @QueryParam("status") String status
			// @formatter:on
	);

	@POST
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation("Create an activation")
	Response create(@ApiParam("Activation request in JSON format") ActivationBean bean);

	@PUT
	@Path("/{requestuuid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation("Update/edit an activation")
	Response edit(
		// @formatter:off
			@ApiParam(value = "Activation request uuid") @PathParam("requestuuid") String requestUuid,
			@ApiParam(value = "to disable or not", allowableValues = "true,false", defaultValue = "false", required = false)
			@QueryParam("disable")
				boolean disable,
			@ApiParam("Activation request in JSON format") ActivationBean bean
			// @formatter:on
	);

	@GET
	@Path("/{requestuuid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation("Retrieve an activation request by uuid")
	public ActivationBean get(
		@ApiParam(value = "Activation request uuid") @PathParam("requestuuid") String requestUuid);

	/**
	 * Get activations
	 *
	 * @param uuid
	 * @param version
	 * @return
	 */
	@GET
	@Path("/item/{uuid}/{version}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get activations for a particular item")
	public Response getActivatedItems(
		// @formatter:off
			@ApiParam(value = "Item uuid", required = true)
				@PathParam("uuid")
				String uuid,
			@ApiParam(value = "Item version", required = true)
				@PathParam("version")
				int version
			// @formatter:on
	);

	@DELETE
	@Path("/{requestuuid}")
	@ApiOperation("Delete an activation request")
	public Response delete(@ApiParam(value = "Activation request uuid") @PathParam("requestuuid") String requestUuid);
}
