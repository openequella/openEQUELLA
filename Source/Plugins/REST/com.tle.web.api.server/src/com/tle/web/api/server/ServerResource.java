package com.tle.web.api.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import bean.ServerInfo;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * @author Seb
 */
@Bind
@Path("status")
@Api(value = "/status", description = "status")
@Produces({"application/json"})
@Singleton
public class ServerResource
{
	@GET
	@Path("")
	@ApiOperation("Check server health")
	public Response isAlive()
	{
		return Response.ok(new ServerInfo()).build();
	}
}
