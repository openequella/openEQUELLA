package com.tle.core.filesystem;

import java.util.Locale;

import com.tle.common.PathUtils;
import com.tle.common.i18n.LocaleUtils;

@SuppressWarnings("nls")
public class LanguageFile extends LanguagesFile
{
	private static final long serialVersionUID = 1L;

	public static final String ROOT_FILENAME = "ROOT";

	private final String path;

	public LanguageFile(Locale locale)
	{
		path = getFilename(locale);
	}

	private String getFilename(Locale locale)
	{
		return Locale.ROOT.equals(locale) ? ROOT_FILENAME : locale.toString();
	}

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), path);
	}

	public static Locale parseLocale(String filename)
	{
		return ROOT_FILENAME.equals(filename) ? Locale.ROOT : LocaleUtils.parseLocale(filename);
	}
}
