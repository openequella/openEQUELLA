package com.tle.core.services.language;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.tle.beans.Language;
import com.tle.core.filesystem.LanguageFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.remoting.RemoteLanguageService;

/**
 * @author Nicholas Read
 */
public interface LanguageService extends RemoteLanguageService
{
	boolean isRightToLeft(Locale locale);

	ResourceBundle getResourceBundle(Locale locale, String bundleGroup);

	void refreshBundles();

	void deleteLanguagePack(Locale locale);

	void importLanguagePack(String stagingId, String filename) throws IOException;

	LanguageFile importLanguagePack(TemporaryFileHandle staging, InputStream zipIn) throws IOException;

	void exportLanguagePack(Locale locale, OutputStream out) throws IOException;

	List<Locale> listAvailableResourceBundles();

	void setLanguages(Collection<Language> languages);
}