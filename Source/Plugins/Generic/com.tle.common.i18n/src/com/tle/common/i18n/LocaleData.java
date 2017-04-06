package com.tle.common.i18n;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author Nicholas Read
 */
public class LocaleData implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final boolean rightToLeft;
	private final String language;
	private final String country;
	private final String variant;

	// Locale is explicitly rebuilt with getLocale, so the default value (null)
	// is appropriate for deserialization
	private transient Locale locale; // NOSONAR

	public LocaleData(Locale locale, boolean rightToLeft)
	{
		this.locale = locale;
		this.rightToLeft = rightToLeft;

		language = locale.getLanguage();
		country = locale.getCountry();
		variant = locale.getVariant();
	}

	public Locale getLocale()
	{
		if( locale == null )
		{
			locale = new Locale(language, country, variant);
		}
		return locale;
	}

	public boolean isRightToLeft()
	{
		return rightToLeft;
	}
}
