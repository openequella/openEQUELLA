package com.tle.web.api.newuitheme;

import com.tle.web.api.newuitheme.impl.NewUITheme;
import io.swagger.annotations.Api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("themeresource/")
@Api("New UI Theme Resource")
public interface NewUIThemeResource {

	//void setTheme(String theme);
	//void setTheme(NewUITheme theme);
	@GET
	@Path("theme.js")
	@Produces("application/javascript")
	Response retrieveThemeInfo();
}
