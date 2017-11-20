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

package com.tle.web.api.acl.interfaces;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.tle.web.api.interfaces.beans.security.TargetListBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Produces({"application/json"})
@Path("acl/")
@Api(value = "/acl", description = "acl")
public interface AclResource
{
	@GET
	@ApiOperation(value = "Get all institution level acls")
	@Path("/")
	public TargetListBean getEntries();

	@PUT
	@ApiOperation(value = "Set all institution level acls")
	@Path("/")
	public Response setEntries(@ApiParam TargetListBean bean);
}
