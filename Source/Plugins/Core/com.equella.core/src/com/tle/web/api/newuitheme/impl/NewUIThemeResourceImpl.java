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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;


import com.tle.core.filesystem.CustomisationFile;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.exceptions.PrivilegeRequiredException;
import com.tle.web.api.newuitheme.NewUIThemeResource;
import com.tle.core.settings.service.ConfigurationService;

/**
 * @author Samantha Fisher
 */

@Bind(NewUIThemeResource.class)
@Singleton
public class NewUIThemeResourceImpl implements NewUIThemeResource {

	@Inject
	private ConfigurationService configurationService;
	@Inject
	private TLEAclManager tleAclManager;
	@Inject
	FileSystemService fsService;

	private static final String THEME_KEY = "Theme";
	private static final String LOGO_FILENAME = "newLogo.png";
	private static final String PERMISSION_KEY = "EDIT_SYSTEM_SETTINGS";

	private void setTheme(String themeString) {
		configurationService.setProperty(THEME_KEY, themeString);
	}

	private void setTheme(NewUITheme theme) throws JsonProcessingException {
		setTheme(theme.toJSONString());
	}

	@GET
	@Path("theme.js")
	@Produces("application/javascript")
	public Response retrieveThemeInfo() throws IOException {
		String themeString = configurationService.getProperty(THEME_KEY);
		//set default theme if none exists in database
		if (themeString == null) {
			setTheme(new NewUITheme());
		}
		return Response.ok("var themeSettings = " + themeString).build();
	}

	@PUT
	@Path("/update")
	public Response updateThemeInfo(NewUITheme theme) throws JsonProcessingException {
		if (tleAclManager.filterNonGrantedPrivileges(Collections.singleton(PERMISSION_KEY), false).isEmpty()) {
			throw new PrivilegeRequiredException(PERMISSION_KEY);
		}
		setTheme(theme);
		return Response.accepted().build();
	}

	@PUT
	@Path("/updatelogo")
	public Response updateLogo(File logoFile) throws IOException {
		if (tleAclManager.filterNonGrantedPrivileges(Collections.singleton(PERMISSION_KEY), false).isEmpty()) {
			throw new PrivilegeRequiredException(PERMISSION_KEY);
		}
		CustomisationFile customisationFile = new CustomisationFile();
		//read in image file
		BufferedImage bImage = null;
		bImage = ImageIO.read(logoFile);
		if (bImage == null) {
			throw new BadRequestException("Invalid image file");
		}

		//resize image to logo size (230px x 36px)
		BufferedImage resizedImage = new BufferedImage(230, 36, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) resizedImage.getGraphics();
		g2d.drawImage(bImage, 0, 0, resizedImage.getWidth() - 1, resizedImage.getHeight() - 1, 0, 0,
			bImage.getWidth() - 1, bImage.getHeight() - 1, null);
		g2d.dispose();
		RenderedImage rImage = resizedImage;

		//write resized image to image file in the institution's filestore
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(rImage, "png", os);
		InputStream fis = new ByteArrayInputStream(os.toByteArray());
		fsService.write(customisationFile, LOGO_FILENAME, fis, false);

		return Response.accepted().build();
	}

	@DELETE
	@Path("/resetlogo")
	public Response resetLogo() {
		if (tleAclManager.filterNonGrantedPrivileges(Collections.singleton(PERMISSION_KEY), false).isEmpty()) {
			throw new PrivilegeRequiredException(PERMISSION_KEY);
		}
		CustomisationFile customisationFile = new CustomisationFile();
		if (fsService.removeFile(customisationFile, LOGO_FILENAME)) {
			return Response.accepted().build();
		} else {
			return Response.notModified().build();
		}
	}

	@GET
	@Path("newLogo.png")
	@Produces("image/png")
	public Response retrieveLogo() throws IOException {
		CustomisationFile customisationFile = new CustomisationFile();
		if (fsService.fileExists(customisationFile, LOGO_FILENAME)) {
			return Response.ok(fsService.read(customisationFile, LOGO_FILENAME), "image/png").build();
		}
		return Response.status(404).build();
	}

	@GET
	@Path("customlogo.js")
	@Produces("application/javascript")
	public Response customLogoExists() {
		CustomisationFile customisationFile = new CustomisationFile();
		if (fsService.fileExists(customisationFile, LOGO_FILENAME)) {
			return Response.ok().entity("var isCustomLogo = true").build();
		} else {
			return Response.ok().entity("var isCustomLogo = false").build();
		}
	}
}
