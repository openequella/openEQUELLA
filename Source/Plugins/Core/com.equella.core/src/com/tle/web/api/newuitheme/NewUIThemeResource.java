package com.tle.web.api.newuitheme;

import com.tle.web.api.newuitheme.impl.NewUITheme;
import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.File;

@Path("themeresource/")
@Api("New UI Theme Resource")
public interface NewUIThemeResource {

	//void setTheme(String theme);
	//void setTheme(NewUITheme theme);
	@GET
	@Path("theme.js")
	@Produces("application/javascript")
	Response retrieveThemeInfo();
	@GET
	@Path("newLogo.png")
	@Produces("image/png")
	Response retrieveLogo();
	@GET
	@Path("customlogo.js")
	@Produces("application/javascript")
	Response customLogoExists();
	@PUT
	@Path("/update")
	Response updateThemeInfo(String themeString);
	@PUT
	@Path("/updatelogo")
	Response updateLogo(File logo);
	@DELETE
	@Path("/resetlogo")
	Response resetLogo();
}
