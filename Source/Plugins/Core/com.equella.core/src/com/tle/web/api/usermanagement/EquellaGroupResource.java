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

package com.tle.web.api.usermanagement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.users.interfaces.GroupResource;
import com.tle.web.api.users.interfaces.beans.GroupBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Aaron
 */

@Produces(MediaType.APPLICATION_JSON)
@Path("usermanagement/local/group/")
@Api(value = "/usermanagement/local/group", description = "usermanagement-local-group")
public interface EquellaGroupResource extends GroupResource
{
	// @formatter:off
	@GET
	@Path("/")
	@ApiOperation("List groups")
	SearchBean<GroupBean> list(
		@Context 
			UriInfo uriInfo,
		@ApiParam(name = "q", required = false) 
		@QueryParam("q") 
			String q,
		@ApiParam(value = "The uuid of the user", required = false)
		@QueryParam("user") 
			String userId,
		@ApiParam(value = "Include parent groups", allowableValues = "true,false", defaultValue = "true", required = false)
		@QueryParam("allParents") 
			Boolean allParents,
		@ApiParam(value = "Peform an exact search by group name",  required = false)
		@QueryParam("name")
			String name
		);
	// @formatter:on		
}
