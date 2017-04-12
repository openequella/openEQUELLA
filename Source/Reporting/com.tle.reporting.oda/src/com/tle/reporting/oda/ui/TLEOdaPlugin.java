package com.tle.reporting.oda.ui;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class TLEOdaPlugin extends Plugin
{
	private static TLEOdaPlugin plugin;
	private ResourceBundle resourceBundle;
	private ResourceBundle resourceBundle2;

	/**
	 * The key for drivers map property in preference store.
	 */
	public static final String DRIVER_MAP_PREFERENCE_KEY = "JDBC Driver Map"; //$NON-NLS-1$
	/**
	 * The key for JAR files map property in preference store.
	 */
	public static final String JAR_MAP_PREFERENCE_KEY = "JDBC Jar List"; //$NON-NLS-1$

	/**
	 * The key for deleted Jar files map property in preference store.
	 */
	public static final String DELETED_JAR_MAP_PREFERENCE_KEY = "Deleted Jar List"; //$NON-NLS-1$

	public TLEOdaPlugin()
	{
		super();
		plugin = this;
		try
		{
			resourceBundle = ResourceBundle.getBundle("com.tle.reporting.oda.i18n.PluginResources");
			resourceBundle2 = ResourceBundle
				.getBundle("org.eclipse.birt.report.data.oda.jdbc.ui.nls.JdbcPluginResources");
		}
		catch( MissingResourceException x )
		{
			resourceBundle = null;
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception
	{
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static TLEOdaPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key)
	{
		ResourceBundle bundle = TLEOdaPlugin.getDefault().getResourceBundle();
		try
		{
			return (bundle != null) ? bundle.getString(key) : key;
		}
		catch( MissingResourceException e )
		{
			return key;
		}
	}

	/**
	 * Returns the string from the Resource bundle, formatted according to the
	 * arguments specified
	 */
	public static String getFormattedString(String key, Object[] arguments)
	{
		return MessageFormat.format(getResourceString(key), arguments);
	}

	public static String getResourceString2(String key)
	{
		ResourceBundle bundle = TLEOdaPlugin.getDefault().getResourceBundle2();
		try
		{
			return (bundle != null) ? bundle.getString(key) : key;
		}
		catch( MissingResourceException e )
		{
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle()
	{
		return resourceBundle;
	}

	public ResourceBundle getResourceBundle2()
	{
		return resourceBundle2;
	}

	public static String getFormattedString2(String key, Object[] arguments)
	{
		return MessageFormat.format(getResourceString2(key), arguments);
	}

}
