package com.tle.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale.AbstractCurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class ClientLocaleImplementation extends AbstractCurrentLocale
{
	private final Locale locale;
	private final Properties text;
	private final String keyPrefix;
	private final boolean rtl;

	public ClientLocaleImplementation(URL serverUrl, String[] bundleGroups, Locale locale, boolean rtl)
		throws IOException
	{
		this(serverUrl, bundleGroups, locale, "", rtl);
	}

	public ClientLocaleImplementation(URL serverUrl, String[] bundleGroups, Locale locale, String keyPrefix, boolean rtl)
		throws IOException
	{
		this.locale = locale;
		this.keyPrefix = keyPrefix;
		this.rtl = rtl;

		final Logger logger = Logger.getLogger(getClass().getName());

		text = new Properties();
		for( String bundleGroup : bundleGroups )
		{
			Properties props = new Properties();
			URL textUrl = new URL(serverUrl, "language/download/" + locale + '/' + bundleGroup + ".properties");

			logger.info("Reading " + textUrl);
			InputStream in = null;
			try
			{
				in = textUrl.openStream();
				props.load(in);
			}
			finally
			{
				if( in != null )
				{
					in.close();
				}
			}
			text.putAll(props);
		}

		logger.info("Read " + text.keySet().size() + " strings");
	}

	public ClientLocaleImplementation(Locale locale, Properties text, String keyPrefix, boolean rtl)
	{
		this.locale = locale;
		this.keyPrefix = keyPrefix;
		this.text = text;
		this.rtl = rtl;
	}

	@Override
	public Locale getLocale()
	{
		return locale;
	}

	@Override
	protected Pair<Locale, String> resolveKey(final String key)
	{
		final String k = keyPrefix + key;
		String property = text.getProperty(k);
		if( property == null )
		{
			throw new MissingResourceException(k, k, k);
		}
		return new Pair<Locale, String>(locale, property);
	}

	@Override
	public boolean isRightToLeft()
	{

		return rtl;
	}

	@Override
	public ResourceBundle getResourceBundle()
	{
		throw new UnsupportedOperationException();
	}
}
