package com.tle.web.api.newUItheme.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.core.guice.Bind;
import com.tle.web.api.newUItheme.NewUIThemeResource;
import io.swagger.annotations.Api;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import net.sf.json.JSONObject;

import java.io.IOException;


@Bind(NewUIThemeResource.class)
@Singleton
public class NewUIThemeResourceImpl implements NewUIThemeResource {
	@Inject
	private ConfigurationService configurationService;
	private NewUITheme theme;
	public void setTheme(String theme) {
		configurationService.setProperty("Theme", theme);

	}

	@GET
	@Path("theme.js")
	@Produces("application/javascript")
	public Response themeInfo() {

		if(configurationService.getProperty("Theme")!=""){
			configurationService.setProperty("Theme",
				"{\"primaryColor\":\"#2196f3\"," +
				"\"secondaryColor\":\"#ff9800\", " +
				"\"backgroundColor\":\"#fafafa\", " +
				"\"menuItemColor\":\"#ffffff\", " +
				"\"menuItemTextColor\": \"#000000\", " +
				"\"fontSize\": 14}");
		}

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			 theme = objectMapper.readValue(configurationService.getProperty("Theme"),NewUITheme.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String themeString = "";
		try {
			themeString = objectMapper.writeValueAsString(theme);
			System.out.println(themeString);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		ObjectExpression expression = new ObjectExpression();
//		expression.put("theme",themeString);
//		expression.put("primaryHex", theme.getPrimaryColor()); //def #2196f3
//		expression.put("secondaryHex", theme.getSecondaryColor()); //def #ff9800
//		expression.put("fontSize", theme.getFontSize()); //def 14
//		expression.put("background", theme.getBackgroundColor()); //def #fafafa
//		expression.put("menuItemBackground", theme.getMenuItemColor()); //def #ffffff
//		expression.put("menuItemTextColor", theme.getMenuItemTextColor()); //def #000000
		return Response.ok("var themeSettings = " + themeString).build();
	}
}
