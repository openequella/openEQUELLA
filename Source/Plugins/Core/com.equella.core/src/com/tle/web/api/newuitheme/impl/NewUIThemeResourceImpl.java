package com.tle.web.api.newuitheme.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.api.newuitheme.NewUIThemeResource;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.tle.core.settings.service.ConfigurationService;

import java.io.IOException;


@Bind(NewUIThemeResource.class)
@Singleton
public class NewUIThemeResourceImpl implements NewUIThemeResource {
	@Inject
	private ConfigurationService configurationService;
	private NewUITheme theme;
	private final String themeKey = "Theme";
	private ObjectMapper objectMapper = new ObjectMapper();

	private void setTheme(String theme) {
		configurationService.setProperty(themeKey, theme);
	}

	private void setTheme(NewUITheme theme) {
		setTheme(themeToString(theme));
	}

	private String themeToString(NewUITheme theme) {
		String themeToString = "";
		try {
			themeToString = objectMapper.writeValueAsString(theme);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return themeToString;
	}

	@GET
	@Path("theme.js")
	@Produces("application/javascript")
	public Response retrieveThemeInfo() {

		if (configurationService.getProperty(themeKey) == null) {    //set default theme if none exists in database
			setTheme(new NewUITheme());
			System.out.println("No theme information found in database. Setting default theme...");
		}
		try {
			theme = objectMapper.readValue(configurationService.getProperty("Theme"), NewUITheme.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Response.ok("var themeSettings = " + themeToString(theme)).build();
	}
	@PUT
	@Path("/update")
	public Response updateThemeInfo(String themeString){
		setTheme(themeString);
		return Response.ok("{}").build();
	}


}
