/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.api.activation;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.tle.web.api.interfaces.BaseEntityResource;
import com.tle.web.api.interfaces.beans.PagingBean;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;

@Path("course")
@Api(value = "Courses", description = "course")
@Produces(MediaType.APPLICATION_JSON)
public interface CourseResource extends BaseEntityResource<CourseBean, BaseEntitySecurityBean>
{
	@GET
	@ApiOperation("List all courses")
	PagingBean<CourseBean> list(@Context UriInfo uriInfo,
		   @ApiParam(value = "Course code to search for", required = false) @QueryParam("code") String code,
		   @ApiParam("Search name and description") @QueryParam("q") String q,
		   @ApiParam("Include archived") @QueryParam("archived") @DefaultValue("false") boolean includeArchived,
		   @ApiParam("Privilege(s) to filter by") @QueryParam("privilege") List<String> privilege,
		   @QueryParam("resumption") @ApiParam("Resumption token for paging") String resumption,
		   @QueryParam("length") @ApiParam("Number of results") @DefaultValue("10") int length,
		   @QueryParam("full") @ApiParam("Return full entity (needs VIEW or EDIT privilege)") boolean full);

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

	@GET
	@Path("/bycode/{code}")
	@ApiOperation("Get a course")
	public CourseBean getByCode(@Context UriInfo uriInfo, @PathParam("code") String code);

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
