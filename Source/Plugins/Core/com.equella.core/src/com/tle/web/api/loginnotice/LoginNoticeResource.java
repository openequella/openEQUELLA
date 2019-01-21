package com.tle.web.api.loginnotice;

import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * @author Samantha Fisher
 */

@Path("loginnotice/")
@Api("Login Notice")
public interface LoginNoticeResource
{
	@GET
	@Produces("text/plain")
	@Path("settings")
	Response retrieveNotice();

	@PUT
	@Path("settings")
	Response setNotice(String loginNotice);

	@DELETE
	@Path("settings")
	Response deleteNotice();
}
