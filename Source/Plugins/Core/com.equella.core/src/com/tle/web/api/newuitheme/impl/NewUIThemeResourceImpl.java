package com.tle.web.api.newuitheme.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.filesystem.CustomisationFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.web.api.newuitheme.NewUIThemeResource;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.tle.core.settings.service.ConfigurationService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Collections;


@Bind(NewUIThemeResource.class)
@Singleton
public class NewUIThemeResourceImpl implements NewUIThemeResource {
	@Inject
	private ConfigurationService configurationService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private TLEAclManager tleAclManager;
	@Inject
	FileSystemService fsService;
	private CustomisationFile customisationFile = new CustomisationFile();
	private ImageObserver observer;
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
	public Response updateThemeInfo(String themeString) {
		if (!tleAclManager.filterNonGrantedPrivileges(Collections.singleton("EDIT_SYSTEM_SETTINGS"), false).isEmpty()) {
			setTheme(themeString);
			return Response.ok("{}").build();
		} else {
			return Response.status(403, "Current user not authorized to modify theme settings").build();
		}
	}

	@PUT
	@Path("/updatelogo")
	public Response updateLogo(File logo) {
		System.out.println("FROM REST: " + CurrentInstitution.get());
		if (!tleAclManager.filterNonGrantedPrivileges(Collections.singleton("EDIT_SYSTEM_SETTINGS"), false).isEmpty()) {
			customisationFile.
				setInstitution(
					CurrentInstitution.get())
			;
			BufferedImage bImage = null;
			try {
				bImage = ImageIO.read(logo);
			} catch (IOException e) {
				e.printStackTrace();
			}
			RenderedImage rImage = bImage;
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
		}
		return Response.status(403, "Current user not authorized to modify logo settings").build();
	}

}
