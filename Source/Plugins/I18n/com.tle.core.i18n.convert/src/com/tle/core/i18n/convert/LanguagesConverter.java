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

package com.tle.core.i18n.convert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.Language;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.dao.LanguageDao;
import com.tle.core.i18n.service.LanguageService;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;

@SuppressWarnings("nls")
@Bind
@Singleton
public class LanguagesConverter extends AbstractConverter<Language>
{
	private static final String LANGUAGES_FOLDER = "languages";
	public static final String LANGUAGES_FILE = PathUtils.filePath(LANGUAGES_FOLDER, "languages.xml");

	@Inject
	private LanguageDao languageDao;
	@Inject
	private LanguageService languageService;

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		List<Long> ids = languageDao.enumerateAllIds();
		for( Long id : ids )
		{
			languageDao.delete(languageDao.findById(id));
		}
		languageDao.flush();
		languageDao.clear();

		final List<Locale> locales = languageService.listAvailableResourceBundles();
		for( Locale locale : locales )
		{
			languageService.deleteLanguagePack(locale);
		}
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		List<Language> languages = languageDao.enumerateAll();
		languages = initialiserService.initialise(languages);
		xmlHelper.writeXmlFile(staging, LANGUAGES_FILE, languages);

		final List<Locale> locales = languageService.listAvailableResourceBundles();
		for( Locale locale : locales )
		{
			SubTemporaryFile stemp = new SubTemporaryFile(staging,
				PathUtils.filePath(LANGUAGES_FOLDER, PathUtils.filePath(locale.toString(), "pack.zip")));

			try( OutputStream out = fileSystemService.getOutputStream(stemp, "", false) )
			{
				languageService.exportLanguagePack(locale, out);
			}
		}
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		if( !fileSystemService.fileExists(staging, LANGUAGES_FILE) )
		{
			return;
		}

		final List<Language> languages = xmlHelper.readXmlFile(staging, LANGUAGES_FILE);

		if( Check.isEmpty(languages) )
		{
			Locale locale = CurrentLocale.getLocale();
			Language defaultLang = new Language();
			defaultLang.setLanguage(locale.getLanguage());
			defaultLang.setCountry(locale.getCountry());
			defaultLang.setVariant(locale.getVariant());
			languages.add(defaultLang);
		}

		for( Language language : languages )
		{
			language.setInstitution(institution);
			language.setId(0);
			languageDao.save(language);
		}
		languageDao.flush();
		languageDao.clear();

		SubTemporaryFile langs = new SubTemporaryFile(staging, LANGUAGES_FOLDER);
		for( String file : fileSystemService.grep(langs, "", "*/pack.zip") )
		{
			try( InputStream zip = fileSystemService.read(new SubTemporaryFile(langs, file), null) )
			{
				SubTemporaryFile packFolder = new SubTemporaryFile(langs, PathUtils.getParentFolderFromFilepath(file));
				languageService.importLanguagePack(packFolder, zip);
			}
		}
		languageService.refreshBundles();
	}

	@Override
	public com.tle.core.institution.convert.Converter.ConverterId getConverterId()
	{
		return ConverterId.LANGUAGES;
	}
}
