package com.tle.web.api.users.interfaces;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.users.interfaces.beans.GroupBean;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

// Note: EQUELLA overrides the root Path
@Produces(MediaType.APPLICATION_JSON)
@Path("localgroup/")
@Api(value = "/localgroup", description = "localgroup")
public interface GroupResource
{
	@GET
	@Path("/{uuid}")
	@ApiOperation("Retrieve a group")
	public GroupBean getGroup(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@GET
	@Path("/{uuid}/user")
	@ApiOperation("Get users in a group")
	public SearchBean<UserBean> getUsersInGroup(@Context UriInfo uriInfo, @PathParam("uuid") String uuid,
		@QueryParam("recursive") boolean recursive);

	@PUT
	@Path("/{uuid}")
	@ApiOperation("Edit a group")
	public Response editGroup(@PathParam("uuid") String uuid, @ApiParam GroupBean group);

	@DELETE
	@Path("/{uuid}")
	@ApiOperation("Delete a group")
	public Response deleteGroup(@PathParam("uuid") String uuid);

	@POST
	@Path("/")
	@ApiOperation("Add a group")
	public Response addGroup(@ApiParam GroupBean group);

	@GET
	@Path("/")
	@ApiOperation("List groups")
	public SearchBean<GroupBean> list(@Context UriInfo uriInfo,
		@ApiParam(name = "name", required = false) @QueryParam("name") String name);
}
