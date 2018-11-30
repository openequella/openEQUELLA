package com.tle.core.settings.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tle.core.filesystem.CustomisationFile;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.settings.service.ThemeSettingsService;
import com.tle.exceptions.PrivilegeRequiredException;
import com.tle.web.api.newuitheme.impl.NewUITheme;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Collections;

@Singleton
@SuppressWarnings("nls")
@Bind(ThemeSettingsService.class)
public class ThemeSettingsServiceImpl implements ThemeSettingsService {
	@Inject
	TLEAclManager tleAclManager;
	@Inject
	ConfigurationService configurationService;
	@Inject
	FileSystemService fileSystemService;

	private static PluginResourceHelper helper = ResourcesService.getResourceHelper(ThemeSettingsServiceImpl.class);

	private static final String PERMISSION_KEY = "EDIT_SYSTEM_SETTINGS";
	private static final String LOGO_FILENAME = "newLogo.png";
	private static final String THEME_KEY = "Theme";
	private static final String CUSTOM_LOGO_URI = "/api/theme/" + LOGO_FILENAME;
	private static final String EQUELLA_LOGO_URI = helper.url("images/new-equella-logo.png");

	@Override
	public String getTheme() throws JsonProcessingException {
		String themeString = configurationService.getProperty(THEME_KEY);
		//use default theme if none exists in database
		return themeString.isEmpty() ? new NewUITheme().toJSONString() : themeString;
	}

	@Override
	public String getLogoURI() {
		CustomisationFile customisationFile = new CustomisationFile();
		if (fileSystemService.fileExists(customisationFile, LOGO_FILENAME)) {
			return CUSTOM_LOGO_URI;
		} else {
			return EQUELLA_LOGO_URI;
		}
	}

	@Override
	public InputStream getCustomLogo() throws IOException {
		CustomisationFile customisationFile = new CustomisationFile();
		if (fileSystemService.fileExists(customisationFile, LOGO_FILENAME)) {
			return fileSystemService.read(customisationFile, LOGO_FILENAME);
		}
		return null;
	}

	@Override
	public boolean getCustomLogoStatus() {
		CustomisationFile customisationFile = new CustomisationFile();
		return fileSystemService.fileExists(customisationFile, LOGO_FILENAME);
	}

	@Override
	public boolean setTheme(NewUITheme theme) throws JsonProcessingException {
		checkPermissions();
		String themeString = theme.toJSONString();
		configurationService.setProperty(THEME_KEY, themeString);
		return true;

	}

	@Override
	public boolean setLogo(File logoFile) throws IOException {
		checkPermissions();
		CustomisationFile customisationFile = new CustomisationFile();
		//read in image file
		BufferedImage bImage = null;
		bImage = ImageIO.read(logoFile);
		if (bImage == null) {
			throw new IllegalArgumentException("Invalid image file");
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
		fileSystemService.write(customisationFile, LOGO_FILENAME, fis, false);
		return true;
	}

	@Override
	public boolean deleteLogo() {
		checkPermissions();
		CustomisationFile customisationFile = new CustomisationFile();
		return fileSystemService.removeFile(customisationFile, LOGO_FILENAME);
	}

	private void checkPermissions() {
		if (tleAclManager.filterNonGrantedPrivileges(Collections.singleton(PERMISSION_KEY), false).isEmpty()) {
			throw new PrivilegeRequiredException(PERMISSION_KEY);
		}
	}
}
