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

package com.tle.web.api.staging.interfaces;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.tle.web.api.staging.interfaces.beans.MultipartBean;
import com.tle.web.api.staging.interfaces.beans.MultipartCompleteBean;
import com.tle.web.api.staging.interfaces.beans.StagingBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Jolse
 */
@Produces({"application/json"})
@Path("staging/")
@Api(value = "/staging", description = "staging")
public interface StagingResource
{
	@POST
	@ApiOperation(value = "Create a file area")
	public Response createStaging();

	@GET
	@Path("/{uuid}")
	@ApiOperation(value = "Get a file area listing", response = StagingBean.class)
	public StagingBean getStaging(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@HEAD
	@Path("/{uuid}/{filepath:(.*)}")
	@ApiOperation(value = "Get metadata for file")
	public Response headFile(@PathParam("uuid") String uuid, @PathParam("filepath") String filepath);

	@GET
	@Path("/{uuid}/{filepath:(.*)}")
	@ApiOperation(value = "Read a file")
	public Response getFile(@Context HttpHeaders headers, @PathParam("uuid") String uuid,
		@PathParam("filepath") String filepath);

	@DELETE
	@Path("/{uuid}/{filepath:(.*)}")
	@ApiOperation(value = "Delete a file")
	public Response deleteFile(@PathParam("uuid") String uuid, @PathParam("filepath") String filepath,
		@QueryParam("uploadId") String uploadId) throws IOException;

	@DELETE
	@Path("/{uuid}")
	@ApiOperation(value = "Delete a staging area")
	public Response deleteStaging(@PathParam("uuid") String uuid) throws IOException;

	@POST
	@Path("/{uuid}/{filepath:(.*)}")
	@ApiOperation(value = "Complete a multipart upload")
	@Consumes("application/json")
	public Response completeMultipart(@PathParam("uuid") String uuid, @PathParam("filepath") String filepath,
		@QueryParam("uploadId") String uploadId, MultipartCompleteBean completion) throws IOException;

	@POST
	@Path("/{uuid}/{filepath:(.*)}")
	@ApiOperation(value = "Start a multipart upload", response = MultipartBean.class)
	public MultipartBean startMultipart(@PathParam("uuid") String uuid, @PathParam("filepath") String filepath,
		@QueryParam("uploads") Boolean uploads);

	@PUT
	@Path("/{uuid}/{filepath:(.*)}")
	@ApiOperation(value = "Put a file")
	public Response putFile(
		@PathParam("uuid") String uuid,
		@PathParam("filepath") String filepath,
		InputStream data,
		@ApiParam("folder to unzip to") @QueryParam("unzipto") String unzipTo,
		@ApiParam("path of existing file to copy from") @QueryParam("copyfrom") String copySource,
		@ApiParam(value = "part number for multipart upload", allowableValues = "1-10000") @QueryParam("partNumber") int partNumber,
		@ApiParam("id for multipart upload") @QueryParam("uploadId") String uploadId,
		@HeaderParam("content-length") @DefaultValue("-1") long size, @HeaderParam("content-type") String contentType)
		throws IOException;
}