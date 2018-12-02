/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.api.newuitheme.impl;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.tle.core.guice.Bind;
import com.tle.core.settings.service.ThemeSettingsService;
import com.tle.web.api.newuitheme.NewUIThemeResource;

/**
 * @author Samantha Fisher
 */

@Bind(NewUIThemeResource.class)
@Singleton
public class NewUIThemeResourceImpl implements NewUIThemeResource {

	@Inject
	ThemeSettingsService themeSettingsService;

	@GET
	@Path("theme.js")
	@Produces("application/javascript")
	public Response retrieveThemeInfo() throws IOException {
		String themeString = themeSettingsService.getTheme();
		return Response.ok("var themeSettings = " + themeString).build();
	}

	@GET
	@Path("logopath")
	@Produces("text/plain")
	public Response retrieveLogoPath() {
		return Response.ok(themeSettingsService.getLogoURI()).build();
	}

	@PUT
	@Path("/update")
	public Response updateThemeInfo(NewUITheme theme) throws JsonProcessingException {
		themeSettingsService.setTheme(theme);
		return Response.accepted().build();
	}

	@PUT
	@Path("/updatelogo")
	public Response updateLogo(File logoFile) throws IOException {
		themeSettingsService.setLogo(logoFile);
		return Response.accepted().build();
	}

	@DELETE
	@Path("/resetlogo")
	public Response resetLogo() {
		themeSettingsService.deleteLogo();
		return Response.accepted().build();
	}

	@GET
	@Path("newLogo.png")
	@Produces("image/png")
	public Response retrieveLogo() throws IOException {
		return Response.ok(themeSettingsService.getCustomLogo(), "image/png").build();
	}

	@GET
	@Path("customlogo.js")
	@Produces("application/javascript")
	public Response customLogoExists() {
		return Response.ok().entity("var isCustomLogo = " + themeSettingsService.getCustomLogoStatus()).build();
	}
}
