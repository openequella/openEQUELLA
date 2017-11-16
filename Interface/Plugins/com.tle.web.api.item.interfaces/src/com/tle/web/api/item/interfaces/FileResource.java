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

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.cache.Cache;

import com.tle.web.api.item.interfaces.beans.GenericFileBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Produces({"application/json"})
@Path("file/")
@Api(value = "/file", description = "file")
@SuppressWarnings("nls")
public interface FileResource
{
	static final String APIDOC_UNZIPTO = "If a zip file is uploaded, unzip it to this location";

	@POST
	@ApiOperation(value = "Create a file area")
	public Response createStaging();

	@POST
	@Path("/copy")
	@ApiOperation(value = "Copy an item's files to a new file area")
	public Response createStagingFromItem(@QueryParam("uuid") String itemUuid, @QueryParam("version") int itemVersion)
		throws IOException;

	@GET
	@Path("/{uuid}/dir{filename:(/.*)?}")
	@ApiOperation(value = "Get file metadata")
	public GenericFileBean getFileMetadata(
//@formatter:off
		@PathParam("uuid") String stagingUuid,
		@PathParam("filename") @ApiParam(value="The folder path, or '/' for root") String filename,
		@ApiParam(name="deep", allowableValues = ",true,false", defaultValue="false", required=false, value="Recurse into subfolders") 
			@QueryParam("deep") Boolean deep) throws IOException;
		//@formatter:on

	@POST
	@Path("/{uuid}/dir{parentFolder:(/.*)?}")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create a folder")
	public Response createFolderPost(//@formatter:off
		@PathParam("uuid") String stagingUuid,
		@PathParam("parentFolder") @ApiParam(value="The parent folder, or '/' for root") String parentFolder,
		@ApiParam(value="The folder definition") GenericFileBean folder
		//@formatter:on
	);

	@PUT
	@Path("/{uuid}/dir{parentFolder:(/.*)?}/{foldername}")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create or rename a folder")
	public Response createOrRenameFolderPut(
		//@formatter:off
		@PathParam("uuid") 
			String stagingUuid,
		@ApiParam(required = false, value="The parent folder, or '/' for root") 
		@PathParam("parentFolder") 
			String parentFolder,
		@PathParam("foldername") 
			String foldername,
		@ApiParam(value="The folder definition") 
			GenericFileBean fileOrFolder
		//@formatter:on
	) throws IOException;

	@PUT
	@Path("/{uuid}/dir{parentFolder:(/.*)?}/{foldername}")
	@Consumes(MediaType.WILDCARD)
	public Response createFolderPut(
		//@formatter:off
		@PathParam("uuid") String stagingUuid,
		@PathParam("parentFolder") String parentFolder,
		@PathParam("foldername") String foldername
		//@formatter:on
	);

	@DELETE
	@Path("/{uuid}/dir{folder:(/.*)?}")
	@ApiOperation(value = "Delete a folder")
	public Response deleteFolder(@PathParam("uuid") String stagingUuid,
		@ApiParam(required = false, value = "The folder to delete, or '/' for root") @PathParam("folder") String folder)
		throws IOException;

	@GET
	@Path("/{uuid}/content/{filepath:(.*)}")
	@Produces(MediaType.WILDCARD)
	@ApiOperation(value = "Download a file")
	@Cache(maxAge = 86400, sMaxAge = 0, mustRevalidate = true)
	public Response downloadFile(@Context HttpHeaders headers, @PathParam("uuid") String stagingUuid,
		@PathParam("filepath") String filepath) throws IOException;

	@PUT
	@Path("/{uuid}/content/{filepath:(.*)}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Upload or replace a file")
	public Response uploadOrReplaceFile(
		//@formatter:off
		@PathParam("uuid")
			String stagingUuid,
		@PathParam("filepath")
			String filepath,
		@QueryParam("append")
			boolean append,
		@ApiParam(value = APIDOC_UNZIPTO)
		@QueryParam("unzipto")
			String unzipTo,
		@HeaderParam("content-length") @DefaultValue("-1")
			long size,
		@HeaderParam("content-type")
			String contentType,
		InputStream binaryData
		//@formatter:on
	) throws IOException;

	@DELETE
	@Path("/{uuid}/content/{filepath:(.*)}")
	@ApiOperation(value = "Delete a file")
	public Response deleteFile(// @formatter:off
		@PathParam("uuid") String stagingUuid,
		@PathParam("filepath") String filepath
		// @formatter:on
	) throws IOException;
}
