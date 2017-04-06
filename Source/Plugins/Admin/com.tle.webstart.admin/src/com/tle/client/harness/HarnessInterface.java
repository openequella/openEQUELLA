/*
 * Created on 7/11/2005
 */
package com.tle.client.harness;

import java.net.URL;
import java.util.Locale;

import com.tle.admin.PluginServiceImpl;

public interface HarnessInterface
{
	void start();

	void setLocale(Locale locale);

	void setEndpointURL(URL url);

	void setPluginService(PluginServiceImpl pluginService);
}
