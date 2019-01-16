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

package com.tle.web.api.report;

import com.tle.web.api.interfaces.BaseEntityResource;
import com.tle.web.api.interfaces.beans.PagingBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("report/")
@Api(value = "Report management", description = "report")
@Produces({"application/json"})
public interface ReportResource extends BaseEntityResource<ReportBean, BaseEntitySecurityBean> {
  @GET
  @Path("/acl")
  @ApiOperation(value = "List global report acls")
  public BaseEntitySecurityBean getAcls(@Context UriInfo uriInfo);

  @PUT
  @Path("/acl")
  @ApiOperation(value = "Edit global report acls")
  public Response editAcls(@Context UriInfo uriInfo, BaseEntitySecurityBean security);

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Retrieve reports")
  PagingBean<ReportBean> list(
      @Context UriInfo uriInfo,
      @ApiParam("Search name and description") @QueryParam("q") String q,
      @ApiParam("Privilege(s) to filter by") @QueryParam("privilege") List<String> privilege,
      @QueryParam("resumption") @ApiParam("Resumption token for paging") String resumptionToken,
      @QueryParam("length") @ApiParam("Number of results") @DefaultValue("10") int length,
      @QueryParam("full") @ApiParam("Return full entity (needs VIEW or EDIT privilege)")
          boolean full);

  @GET
  @Path("/{uuid}")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation("Retrieve a report by uuid")
  public ReportBean get(
      @Context UriInfo uriInfo, @ApiParam(value = "Report uuid") @PathParam("uuid") String uuid);

  @POST
  @Path("/{uuid}/package")
  @ApiOperation("Package a report's design files into a staging folder")
  public Response packageReportFiles(
      @Context UriInfo uriInfo, @ApiParam(value = "Report uuid") @PathParam("uuid") String uuid);

  /**
   * @param uriInfo
   * @param bean
   * @param stagingUuid
   * @param packageFilename optional - defaults to bean.getFilename(). Only needed if the report is
   *     zipped.
   * @return
   */
  @POST
  @ApiOperation("Create a new report")
  public Response create(
      @Context UriInfo uriInfo,
      @ApiParam ReportBean bean,
      @ApiParam(required = false) @QueryParam("staginguuid") String stagingUuid,
      @ApiParam(required = false) @QueryParam("packagename") String packageFilename);

  @PUT
  @Path("/{uuid}")
  @ApiOperation(value = "Edit a report")
  public Response edit(
      @Context UriInfo uriInfo,
      @PathParam("uuid") String uuid,
      @ApiParam ReportBean bean,
      @ApiParam(required = false) @QueryParam("staginguuid") String staginguuid,
      @ApiParam(required = false) @QueryParam("packagename") String packagename,
      @ApiParam(required = false) @QueryParam("lock") String lockId,
      @ApiParam(required = false) @QueryParam("keeplocked") boolean keepLocked);

  @DELETE
  @Path("/{uuid}")
  @ApiOperation("Delete a report")
  public Response delete(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

  @GET
  @Path("/{uuid}/lock")
  @ApiOperation("Read the lock for a report")
  public Response getLock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

  @POST
  @Path("/{uuid}/lock")
  @ApiOperation("Lock a report")
  public Response lock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

  @DELETE
  @Path("/{uuid}/lock")
  @ApiOperation("Unlock a report")
  public Response unlock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);
}
