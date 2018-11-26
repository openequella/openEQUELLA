package com.tle.web.api.newuitheme.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import com.tle.web.api.newuitheme.NewUIThemeResource;
import com.tle.core.settings.service.ConfigurationService;


@Bind(NewUIThemeResource.class)
@Singleton
public class NewUIThemeResourceImpl implements NewUIThemeResource {

	@Inject
	private ConfigurationService configurationService;
	@Inject
	private TLEAclManager tleAclManager;
	@Inject
	FileSystemService fsService;

	private CustomisationFile customisationFile;
	private NewUITheme theme;
	private static final String THEME_KEY = "Theme";
	private ObjectMapper objectMapper = new ObjectMapper();

	private void setTheme(String themeString) {
		configurationService.setProperty(THEME_KEY, themeString);
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
		//set default theme if none exists in database
		if (configurationService.getProperty(THEME_KEY) == null) {
			System.out.println("No theme information found in database. Setting default theme...");
			setTheme(new NewUITheme());
		}

		try {
			theme = objectMapper.readValue(configurationService.getProperty(THEME_KEY), NewUITheme.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Response.ok("var themeSettings = " + themeToString(theme)).build();
	}

	@PUT
	@Path("/update")
	public Response updateThemeInfo(String themeString) {
		if (!tleAclManager.filterNonGrantedPrivileges(Collections.singleton("EDIT_SYSTEM_SETTINGS"), false).isEmpty()) {
			setTheme(themeString);
			return Response.ok("{}").build();
		} else {
			return Response.status(403).entity("{\"reason\":\"Current user not authorized to modify theme settings\"}").build();
		}
	}

	@PUT
	@Path("/updatelogo")
	public Response updateLogo(File logoFile) {
		if (!tleAclManager.filterNonGrantedPrivileges(Collections.singleton("EDIT_SYSTEM_SETTINGS"), false).isEmpty()) {
			customisationFile = new CustomisationFile();

			//read in image file
			BufferedImage bImage = null;
			try {
				bImage = ImageIO.read(logoFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//resize image to logo size (230px x 36px)
			BufferedImage resizedImage = new BufferedImage(230, 36, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D) resizedImage.getGraphics();
			if (bImage == null) {
				return Response.notModified().entity("Invalid logo image").build();
			} else {
				g2d.drawImage(bImage, 0, 0, resizedImage.getWidth() - 1, resizedImage.getHeight() - 1, 0, 0,
					bImage.getWidth() - 1, bImage.getHeight() - 1, null);
				g2d.dispose();
			}
			RenderedImage rImage = resizedImage;
			//write resized image to image file in the institution's filestore
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				ImageIO.write(rImage, "png", os);
			} catch (IOException e) {
				e.printStackTrace();
			}

			InputStream fis = new ByteArrayInputStream(os.toByteArray());

			try {
				fsService.write(customisationFile, "newLogo.png", fis, false);

			} catch (IOException e) {
				e.printStackTrace();
			}

			return Response.ok("{}").build();
		} else {
			return Response.status(403).entity("{\"reason\":\"Current user not authorized to modify logo settings\"}").build();
		}
	}

	@DELETE
	@Path("/resetlogo")
	public Response resetLogo() {
		customisationFile = new CustomisationFile();
		if (fsService.removeFile(customisationFile, "newLogo.png")) {
			return Response.ok("{}").build();
		} else {
			return Response.notModified().build();
		}
	}

	@GET
	@Path("newLogo.png")
	@Produces("image/png")
	public Response retrieveLogo() {
		customisationFile = new CustomisationFile();
		try {
			if (fsService.fileExists(customisationFile, "newLogo.png")) {
				return Response.ok(fsService.read(customisationFile, "newLogo.png"), "image/png").build();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Response.status(404).build();
	}

	@GET
	@Path("customlogo.js")
	@Produces("application/javascript")
	public Response customLogoExists() {
		customisationFile = new CustomisationFile();
		if (fsService.fileExists(customisationFile, "newLogo.png")) {
			return Response.ok().entity("var isCustomLogo = true").build();
		} else {
			return Response.ok().entity("var isCustomLogo = false").build();
		}
	}
}
