package com.tle.web.api.newuitheme.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.api.newuitheme.NewUIThemeResource;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
//		configurationService.setProperty("Theme",
//			"{\"primaryColor\":\"#000000\"," +
//				"\"secondaryColor\":\"#FF0000\", " +
//				"\"backgroundColor\":\"#1f1fAA\", " +
//				"\"menuItemColor\":\"#AAAABB\", " +
//				"\"menuItemTextColor\": \"#BF0000\", " +
//				"\"fontSize\": 14}");

		try {
			theme = objectMapper.readValue(configurationService.getProperty("Theme"), NewUITheme.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Response.ok("var themeSettings = " + themeToString(theme)).build();
	}

}
