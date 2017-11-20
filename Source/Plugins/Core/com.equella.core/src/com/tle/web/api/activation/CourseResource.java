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
import javax.ws.rs.core.UriInfo;

import com.tle.web.api.interfaces.BaseEntityResource;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("course")
@Api(value = "/course", description = "course")
@Produces(MediaType.APPLICATION_JSON)
public interface CourseResource extends BaseEntityResource<CourseBean, BaseEntitySecurityBean>
{
	@GET
	@ApiOperation("List all courses")
	public SearchBean<CourseBean> list(@Context UriInfo uriInfo,
		@ApiParam(value = "Course code to search for", required = false) @QueryParam("code") String code);

	@GET
	@Path("/acl")
	@ApiOperation(value = "List global course ACLs")
	public BaseEntitySecurityBean getAcls(@Context UriInfo uriInfo);

	@PUT
	@Path("/acl")
	@ApiOperation(value = "Edit global course ACLs")
	public Response editAcls(@Context UriInfo uriInfo, BaseEntitySecurityBean security);

	@GET
	@Path("/{uuid}")
	@ApiOperation("Get a course")
	public CourseBean get(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@DELETE
	@Path("/{uuid}")
	@ApiOperation("Delete a course")
	public Response delete(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@POST
	@ApiOperation("Create a new course")
	public Response create(@Context UriInfo uriInfo, @ApiParam CourseBean bean, @QueryParam("file") String stagingUuid);

	@PUT
	@Path("/{uuid}")
	@ApiOperation("Edit a course")
	public Response edit(@Context UriInfo uriInfo, @ApiParam @PathParam("uuid") String uuid, @ApiParam CourseBean bean,
		@ApiParam(required = false) @QueryParam("file") String stagingUuid,
		@ApiParam(required = false) @QueryParam("lock") String lockId,
		@ApiParam(required = false) @QueryParam("keeplocked") boolean keepLocked);

	@GET
	@Path("/{uuid}/lock")
	@ApiOperation("Read the lock for a workflow")
	public Response getLock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@POST
	@Path("/{uuid}/lock")
	@ApiOperation("Lock a workflow")
	public Response lock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@DELETE
	@Path("/{uuid}/lock")
	@ApiOperation("Unlock a workflow")
	public Response unlock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);
}
