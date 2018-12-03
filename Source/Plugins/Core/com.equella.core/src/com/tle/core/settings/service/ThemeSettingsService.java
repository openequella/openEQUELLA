package com.tle.core.settings.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tle.web.api.newuitheme.impl.NewUITheme;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface ThemeSettingsService {

	//GETTERS//
	String getTheme() throws JsonProcessingException;
	String getLogoURL();
	InputStream getCustomLogo() throws IOException;
	boolean getCustomLogoStatus();

	//SETTERS (returns true if successful)//
	boolean setTheme(NewUITheme theme) throws JsonProcessingException;
	boolean setLogo(File logoFile) throws IOException;

	//DELETE (returns true if successful//
	boolean deleteLogo();
}
