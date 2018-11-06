package com.tle.web.api.newUItheme;

import com.tle.core.guice.Bind;
import com.tle.web.api.newUItheme.impl.NewUIThemeResourceImpl;
import io.swagger.annotations.Api;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;


@Path("themeresource/")
@Api("Test rest")
//@Bind(NewUIThemeResourceImpl.class)
//@Singleton
public interface NewUIThemeResource {

	void setTheme(String theme);
	@GET
	@Path("theme.js")
	@Produces("application/javascript")
	public Response themeInfo();
}
