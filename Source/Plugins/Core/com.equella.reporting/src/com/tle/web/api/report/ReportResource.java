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
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("report/")
@Api(value = "Report management", description = "report")
@Produces({"application/json"})
public interface ReportResource  extends BaseEntityResource<ReportBean, BaseEntitySecurityBean>
{
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve reports")
    SearchBean<ReportBean> list(@Context UriInfo uriInfo);

    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Retrieve a report by uuid")
    public ReportBean get(@Context UriInfo uriInfo, @ApiParam(value = "Report uuid") @PathParam("uuid") String uuid);

//    @GET
//    @Path("/{uuid}/design")
//    @Produces(MediaType.APPLICATION_OCTET_STREAM)
//    @ApiOperation("Retrieve a report's design files by uuid")
//    public ReportBean get(@Context UriInfo uriInfo, @ApiParam(value = "Report uuid") @PathParam("uuid") String uuid);
}