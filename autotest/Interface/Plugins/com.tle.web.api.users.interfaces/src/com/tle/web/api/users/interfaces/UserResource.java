package com.tle.web.api.users.interfaces;

import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.UserBean;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import javax.ws.rs.Consumes;
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

// Note: EQUELLA overrides the root Path
@Produces(MediaType.APPLICATION_JSON)
@Path("localuser/")
@Api(value = "/localuser", description = "localuser")
public interface UserResource {
  @GET
  @Path("/{uuid}")
  @ApiOperation("Retrieve a user")
  public UserBean getUser(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

  @GET
  @Path("/username/{username}")
  @ApiOperation("Retrieve a user by username")
  public UserBean getUserByUsername(
      @Context UriInfo uriInfo, @PathParam("username") String username);

  @PUT
  @Path("/{uuid}")
  @ApiOperation("Edit a user")
  public Response editUser(@PathParam("uuid") String uuid, @ApiParam UserBean user);

  @DELETE
  @Path("/{uuid}")
  @ApiOperation("Delete a user")
  public Response deleteUser(@PathParam("uuid") String uuid);

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiOperation("Add a user")
  public Response addUser(@ApiParam UserBean user);

  @GET
  @Path("/")
  @ApiOperation("List internal users")
  public SearchBean<UserBean> list(
      @Context UriInfo uriInfo,
      @ApiParam(required = false) @QueryParam("q") String query,
      @ApiParam(required = false) @QueryParam("group") String parentGroupId,
      @ApiParam(
              value = "Search the specified group's child groups as well",
              required = false,
              defaultValue = "false",
              allowableValues = "true|false")
          @QueryParam("recursive")
          boolean recursive);
}
