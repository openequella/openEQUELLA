/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.tle.web.api.staging.interfaces.beans.MultipartBean;
import com.tle.web.api.staging.interfaces.beans.MultipartCompleteBean;
import com.tle.web.api.staging.interfaces.beans.StagingBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

@Produces({"application/json"})
@Path("staging/")
@Api(value = "Staging files", description = "staging")
public interface StagingResource {
  @POST
  @ApiOperation(value = "Create a file area")
  Response createStaging();

  @GET
  @Path("/{uuid}")
  @ApiOperation(value = "Get a file area listing", response = StagingBean.class)
  StagingBean getStaging(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

  @HEAD
  @Path("/{uuid}/{filepath:(.*)}")
  @ApiOperation(value = "Get metadata for file")
  Response headFile(@PathParam("uuid") String uuid, @PathParam("filepath") String filepath);

  @GET
  @Path("/{uuid}/{filepath:(.*)}")
  @ApiOperation(value = "Read a file")
  Response getFile(
      @Context HttpHeaders headers,
      @PathParam("uuid") String uuid,
      @PathParam("filepath") String filepath);

  @DELETE
  @Path("/{uuid}/{filepath:(.*)}")
  @ApiOperation(value = "Delete a file")
  Response deleteFile(@PathParam("uuid") String uuid, @PathParam("filepath") String filepath)
      throws IOException;

  @DELETE
  @Path("/{uuid}")
  @ApiOperation(value = "Delete a staging area")
  Response deleteStaging(@PathParam("uuid") String uuid) throws IOException;

  @POST
  @Path("/{uuid}/{filepath:(.*)}/complete")
  @ApiOperation(value = "Complete a multipart upload")
  @Consumes("application/json")
  Response completeMultipart(
      @PathParam("uuid") String uuid,
      @PathParam("filepath") String filepath,
      @QueryParam("uploadId") String uploadId,
      MultipartCompleteBean completion)
      throws IOException;

  @POST
  @Path("/{uuid}/multipart")
  @ApiOperation(value = "Start a multipart upload", response = MultipartBean.class)
  Response startMultipart(@PathParam("uuid") String uuid);

  @PUT
  @Path("/{uuid}/multipart/{uploadId}/{partNumber}")
  @ApiOperation(value = "Upload a multipart chunk")
  Response uploadChunk(
      @PathParam("uuid") String uuid,
      @PathParam("uploadId") String uploadId,
      @PathParam("partNumber") int partNumber,
      InputStream data,
      @HeaderParam("content-type") String contentType)
      throws IOException;

  @PUT
  @Path("/{uuid}/{filepath:(.*)}")
  @ApiOperation(value = "Put a file")
  Response putFile(
      @PathParam("uuid") String uuid,
      @PathParam("filepath") String filepath,
      InputStream data,
      @ApiParam("Folder to unzip the uploaded file into. Cannot be combined with 'copyfrom'.")
          @QueryParam("unzipto")
          String unzipTo,
      @ApiParam("Path of an existing staging file to copy from. Cannot be combined with 'unzipto'.")
          @QueryParam("copyfrom")
          String copySource,
      @HeaderParam("content-type") String contentType)
      throws IOException;

  @POST
  @Path("/copy")
  @ApiOperation(value = "Copy an item's files to a new staging area")
  Response createStagingFromItem(
      @ApiParam(value = "UUID of the source item", required = true) @QueryParam("itemUuid")
          String itemUuid,
      @ApiParam(value = "Version of the source item", required = true) @QueryParam("itemVersion")
          int itemVersion);
}
