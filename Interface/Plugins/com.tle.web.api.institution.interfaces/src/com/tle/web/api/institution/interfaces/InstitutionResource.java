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

package com.tle.web.api.institution.interfaces;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.tle.web.api.institution.interfaces.beans.InstitutionBean;
import com.tle.web.api.interfaces.Institutional;
import com.tle.web.api.interfaces.Institutional.Type;
import com.tle.web.api.interfaces.beans.SearchBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Produces({"application/json"})
@Path("institution")
@Api(value = "/institution", description = "institution")
@Institutional(Type.NON_INSTITUTIONAL)
public interface InstitutionResource
{
	@POST
	@Consumes("application/json")
	@ApiOperation(value = "Create a new institution")
	public Response newInstitution(
		@ApiParam(value = "Which schema (-1 is the first available)", defaultValue = "-1", required = false) @DefaultValue("-1") @QueryParam("schemaId") long schemaId,
		@ApiParam(value = "The institution bean") final InstitutionBean institutionBean);

	@DELETE
	@Path("/{uniqueId}")
	@ApiOperation(value = "Delete an institution")
	public Response deleteInstitution(@ApiParam("The intitution unique id") @PathParam("uniqueId") long uniqueId);

	@GET
	@ApiOperation(value = "List institution")
	public SearchBean<InstitutionBean> getInstitutions();

	@GET
	@Path("/{uniqueId}")
	@ApiOperation(value = "Get an institution")
	public InstitutionBean getInstitution(@ApiParam("The intitution unique id") @PathParam("uniqueId") long uniqueId);

	@PUT
	@Path("/{uniqueId}")
	@Consumes("application/json")
	@ApiOperation(value = "Edit an institution")
	public Response editInstitution(@ApiParam("The intitution unique id") @PathParam("uniqueId") long uniqueId,
		@ApiParam(value = "The institution bean") final InstitutionBean institutionBean);
}
