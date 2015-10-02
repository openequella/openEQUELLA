package com.tle.web.api.activation;

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

import com.tle.web.api.interfaces.BaseEntityResource;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("course")
@Api(value = "/course", description = "course")
@Produces(MediaType.APPLICATION_JSON)
public interface CourseResource extends BaseEntityResource<CourseBean, BaseEntitySecurityBean>
{
	@GET
	@ApiOperation("List all courses")
	public SearchBean<CourseBean> list(@Context UriInfo uriInfo,
		@ApiParam(value = "Course code to search for", required = false) @QueryParam("code") String code);

	@Override
	@GET
	@Path("/acl")
	@ApiOperation(value = "List global course ACLs")
	public BaseEntitySecurityBean getAcls(@Context UriInfo uriInfo);

	@Override
	@PUT
	@Path("/acl")
	@ApiOperation(value = "Edit global course ACLs")
	public Response editAcls(@Context UriInfo uriInfo, BaseEntitySecurityBean security);

	@Override
	@GET
	@Path("/{uuid}")
	@ApiOperation("Get a course")
	public CourseBean get(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@Override
	@DELETE
	@Path("/{uuid}")
	@ApiOperation("Delete a course")
	public Response delete(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@Override
	@POST
	@ApiOperation("Create a new course")
	public Response create(@Context UriInfo uriInfo, @ApiParam CourseBean bean, @QueryParam("file") String stagingUuid);

	@Override
	@PUT
	@Path("/{uuid}")
	@ApiOperation("Edit a course")
	public Response edit(@Context UriInfo uriInfo, @ApiParam @PathParam("uuid") String uuid, @ApiParam CourseBean bean,
		@ApiParam(required = false) @QueryParam("file") String stagingUuid,
		@ApiParam(required = false) @QueryParam("lock") String lockId,
		@ApiParam(required = false) @QueryParam("keeplocked") boolean keepLocked);

	@Override
	@GET
	@Path("/{uuid}/lock")
	@ApiOperation("Read the lock for a workflow")
	public Response getLock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@Override
	@POST
	@Path("/{uuid}/lock")
	@ApiOperation("Lock a workflow")
	public Response lock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);

	@Override
	@DELETE
	@Path("/{uuid}/lock")
	@ApiOperation("Unlock a workflow")
	public Response unlock(@Context UriInfo uriInfo, @PathParam("uuid") String uuid);
}
