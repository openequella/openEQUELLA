package com.tle.web.api.acl.interfaces;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.tle.web.api.interfaces.beans.security.TargetListBean;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

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
