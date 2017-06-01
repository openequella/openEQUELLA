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

package com.tle.admin;

import java.awt.Window;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.admin.boot.LoadingDialog;
import com.tle.client.harness.HarnessInterface;
import com.tle.client.impl.ClientLocaleImplementation;
import com.tle.client.impl.ClientServiceImpl;
import com.tle.client.impl.CurrentTimeZoneClientSide;
import com.tle.common.applet.SessionHolder;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.core.remoting.RemoteLanguageService;
import com.tle.i18n.BundleCache;

/**
 * This is the main class that launches the Administration Console.
 * 
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class AdminConsole implements HarnessInterface
{
	private static final Log LOGGER = LogFactory.getLog(AdminConsole.class);

	private static final String DOCUMENTBUILDERFACTORY = "javax.xml.parsers.DocumentBuilderFactory";
	private static final String DEFAULT_XML_PARSER5 = "com.sun.org.apache.xerces.internal.jaxp."
		+ "DocumentBuilderFactoryImpl";

	private static final String ERROR_TITLE = "Error";
	private static final String ERROR_MESSAGE = "There has been an error loading the Administration Console"
		+ "\nPlease consult your System Administrator";

	private Window managementDialog;

	private ClientService clientService;
	private Locale locale;
	private URL endpointURL;
	private PluginServiceImpl pluginService;

	public AdminConsole()
	{
		final String javaVersion = System.getProperty("java.version");
		final String osName = System.getProperty("os.name");

		LOGGER.info("Java version is '" + javaVersion + "'");
		LOGGER.info("OS name is '" + osName + "'");
	}

	protected void initLanguageBundles() throws IOException
	{
		// TODO: change the rtl stuff (if we ever support rtl in admin console)
		LOGGER.info("Locale is " + locale);
		CurrentLocale.initialise(new ClientLocaleImplementation(endpointURL, getBundleGroups(), locale, false));
		CurrentTimeZone.initialise(new CurrentTimeZoneClientSide(TimeZone.getDefault()));
	}

	public String[] getBundleGroups()
	{
		return new String[]{"admin-console", "recipient-selector"};
	}

	@Override
	public void start()
	{
		try
		{
			// Detect the Mac hack param
			String tempDir = System.getProperty("jnlp.java.io.tmpdir");
			if( tempDir != null && !tempDir.equals("") )
			{
				System.setProperty("java.io.tmpdir", tempDir);
			}

			// Initialise server session
			SessionHolder holder = new SessionHolder(endpointURL);
			// Initialise services
			clientService = new ClientServiceImpl(holder);

			// Initialise bundle cache
			BundleCache.initialise(clientService.getService(RemoteLanguageService.class));

			// Make sure we are using the default XML Parser.
			System.setProperty(DOCUMENTBUILDERFACTORY, DEFAULT_XML_PARSER5);

			setupLookAndFeel();
			final LoadingDialog loading = new LoadingDialog("Equella: Administration Console");
			loading.setVisible(true);
			loading.toFront();

			// Initialise language bundle now
			initLanguageBundles();

			// Create the driver interface.
			Driver.create(clientService, pluginService);

			// Create the management dialog.
			managementDialog = new ManagementDialog();

			loading.setVisible(false);
			loading.dispose();

			// Switch the visible windows
			managementDialog.setVisible(true);
			managementDialog.toFront();
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(managementDialog, ERROR_MESSAGE, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
			clientService.stop();
			return;
		}

	}

	/**
	 * Ensures the system's native look and feel is set by default.
	 */
	private void setupLookAndFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch( Exception ex )
		{
			System.err.println("Look And Feel could not be set.");
			ex.printStackTrace();
		}
	}

	@Override
	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	@Override
	public void setEndpointURL(URL endpointURL)
	{
		this.endpointURL = endpointURL;
	}

	@Override
	public void setPluginService(PluginServiceImpl pluginService)
	{
		this.pluginService = pluginService;
	}

}
