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
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

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
