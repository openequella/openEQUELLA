package com.tle.reporting.oda.connectors.ui;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class TLEConnectorsOdaPlugin extends Plugin
{
	private static TLEConnectorsOdaPlugin plugin;
	private ResourceBundle resourceBundle;

	public TLEConnectorsOdaPlugin()
	{
		super();
	}

	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		plugin = this;
		try
		{
			resourceBundle = ResourceBundle.getBundle("com.tle.reporting.oda.connectors.i18n.PluginResources");
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
	public static TLEConnectorsOdaPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or the value of key
	 * if not found.
	 * 
	 * @param key The string key
	 */
	public static String getResourceString(String key)
	{
		ResourceBundle bundle = TLEConnectorsOdaPlugin.getDefault().getResourceBundle();
		try
		{
			return (bundle != null) ? bundle.getString(key) : "???" + key + "???";
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

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle()
	{
		return resourceBundle;
	}
}
