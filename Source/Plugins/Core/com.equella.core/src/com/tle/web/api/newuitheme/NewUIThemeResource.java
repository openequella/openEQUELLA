package com.tle.web.api.newuitheme;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * @author Samantha Fisher
 */

@Path("themeresource/")
@Api("New UI Theme Resource")
public interface NewUIThemeResource {

	@GET
	@Path("theme.js")
	@ApiParam("Grabs the current institution's theme settings from the database.")
	@Produces("application/javascript")
	Response retrieveThemeInfo();

	@GET
	@Path("newLogo.png")
	@ApiParam("Grabs the current institution's logo from the filestore.")
	@Produces("image/png")
	Response retrieveLogo();

	@GET
	@Path("customlogo.js")
	@ApiParam("Checks whether or not this institution is using a custom logo.")
	@Produces("application/javascript")
	Response customLogoExists();

	@PUT
	@Path("update")
	@ApiParam("Changes the theme settings of the current institution with a JSON string.")
	Response updateThemeInfo(String themeString);

	@PUT
	@Path("updatelogo")
	@ApiParam("Takes an image file, resizes it to become a logo and saves it to the filestore.")
	Response updateLogo(File logo);

	@DELETE
	@Path("resetlogo")
	@ApiParam("Resets the institution's logo to the default openEQUELLA logo.")
	Response resetLogo();
}
