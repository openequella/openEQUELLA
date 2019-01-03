/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.appletcommon;

import java.awt.Color;
import java.awt.GridLayout;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.tle.client.ListCookieHandler;
import com.tle.client.impl.ClientLocaleImplementation;
import com.tle.client.impl.ClientServiceImpl;
import com.tle.common.applet.SessionHolder;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LocaleUtils;
import com.tle.web.appletcommon.gui.GlassProgressWorker;

@SuppressWarnings("nls")
public abstract class AbstractAppletLauncher extends JApplet
{
	private static final long serialVersionUID = 4286081617393068197L;

	public static final String PARAMETER_PREFIX = "jnlp.";
	public static final String LOCALE_PARAMETER = PARAMETER_PREFIX + "LOCALE";
	public static final String ENDPOINT_PARAMETER = PARAMETER_PREFIX + "ENDPOINT";
	public static final String COOKIE_PARAMETER = PARAMETER_PREFIX + "COOKIE";
	public static final String RTL_PARAMETER = PARAMETER_PREFIX + "RTL";

	protected final Logger logger = Logger.getLogger(getClass().getName());

	protected GlassProgressWorker<?> worker;
	protected boolean loaded;
	protected Object loadLock = new Object();
	protected ClientService clientService;

	@Override
	public void init()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			setLayout(new GridLayout(1, 1));

			if( worker == null )
			{
				worker = new GlassProgressWorker<JComponent>("Loading...", -1, false)
				{
					@Override
					public JComponent construct() throws Exception
					{
						commonInitialise();

						return initAndCreateRootComponent();
					}

					@Override
					public void finished()
					{
						add(get());
						validate();
						onFinished();
					}

					@Override
					public void exception()
					{
						logErrorStartingApplet(getException());

						JLabel label = new JLabel("Error starting application."
							+ "  Please contact your administrator.");
						label.setHorizontalTextPosition(SwingConstants.CENTER);
						label.setHorizontalAlignment(SwingConstants.CENTER);
						label.setOpaque(true);
						label.setBackground(Color.WHITE);
						add(label);
					}
				};
			}
		}
		catch( Exception ex )
		{
			logErrorStartingApplet(ex);
		}
	}

	@Override
	public void start()
	{
		// start gets called at seemingly random times, so you can't just call
		// worker.start
		// whenever start gets called.
		synchronized( loadLock )
		{
			if( !loaded )
			{
				worker.setComponent(this);
				worker.start();
				loaded = true;
			}
		}

		invalidate();
		validate();
	}

	private void commonInitialise() throws Exception
	{
		String systemName = System.getProperty("os.name");
		logger.info("OS is " + systemName);
		boolean isMac = systemName.toLowerCase().startsWith("mac os x");

		String localeParameter = getParameter(LOCALE_PARAMETER);
		logger.info("Unparsed locale is " + localeParameter);
		final Locale locale = LocaleUtils.parseLocale(localeParameter);
		logger.info("Locale is " + locale);

		boolean rtl = Boolean.parseBoolean(getParameter(RTL_PARAMETER));
		logger.info("Orientation is " + (rtl ? "RTL" : "LTR"));

		final URL endpoint = getEndpoint();

		logger.info("Server URL is " + endpoint);
		clientService = new ClientServiceImpl(new SessionHolder(endpoint));

		if( isMac )
		{
			// Force cookie for Firefox on Mac OS X
			ListCookieHandler lch = new ListCookieHandler();
			lch.setIgnoreCookieOverrideAttempts(true);
			lch.put(endpoint.toURI(),
				Collections.singletonMap("Set-Cookie", lch.splitCookieString(getParameter(COOKIE_PARAMETER))));
			CookieHandler.setDefault(lch);
		}

		logger.info("Initialising language bundles...");

		if( endpoint != null )
		{
			CurrentLocale.initialise(new ClientLocaleImplementation(endpoint, getBundleGroups(), locale,
				getI18nKeyPrefix(), rtl));
		}
		else
		{
			try( InputStream in = new FileInputStream("../resources/lang/i18n.properties") )
			{
				Properties props = new Properties();
				props.load(in);

				CurrentLocale.initialise(new ClientLocaleImplementation(locale, props, getI18nKeyPrefix(), rtl));
			}
		}
	}

	protected abstract JComponent initAndCreateRootComponent() throws Exception;

	protected void onFinished()
	{
		// Nothing
	}

	protected abstract String[] getBundleGroups();

	protected abstract String getI18nKeyPrefix();

	protected final URL getEndpoint()
	{
		String url = getParameter(ENDPOINT_PARAMETER);
		try
		{
			return url == null ? null : new URL(url);
		}
		catch( MalformedURLException e )
		{
			throw new RuntimeException("Error with endpoint URL: " + url, e);
		}
	}

	private void logErrorStartingApplet(Throwable th)
	{
		logger.log(Level.SEVERE, "Error starting applet", th);
	}
}
