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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.tle.admin.controls.ControlRepositoryImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.JpfException;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Version;
import com.dytech.gui.ExceptionDialog;
import com.tle.admin.controls.repository.ControlRepository;
import com.tle.common.Check;
import com.tle.common.applet.SessionHolder;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemotePluginDownloadService;
import com.tle.web.appletcommon.AbstractAppletLauncher;

/**
 * Provides the communications ability for the admin console.
 * 
 * @author Nicholas Read
 */
public final class Driver
{
	private static final Log LOGGER = LogFactory.getLog(Driver.class);
	private static final String COLON = ":"; //$NON-NLS-1$

	private static Driver driver = null;

	private final Version version;
	private final String institutionName;
	private final String loggedInUserID;

	private final ClientService clientService;

	private ControlRepository controlRepository;
	private final PluginServiceImpl pluginService;

	/**
	 * @return The singleton Driver instance or null.
	 */
	public static Driver instance()
	{
		return driver;
	}

	public static Driver create(ClientService clientService, PluginServiceImpl pluginService) throws Exception
	{
		if( driver != null )
		{
			throw new IllegalStateException();
		}

		driver = new Driver(clientService, pluginService);
		return driver;
	}

	@SuppressWarnings("nls")
	private Driver(ClientService clientService, PluginServiceImpl pluginService) throws Exception
	{
		this.clientService = clientService;

		// Setup some initial state.

		SessionHolder session = clientService.getSession();
		loggedInUserID = session.getLoginService().getLoggedInUserId();
		institutionName = clientService.getParameter(AbstractAppletLauncher.PARAMETER_PREFIX + "INSTITUTIONNAME");

		version = Version.load();

		if( pluginService == null )
		{
			LOGGER.info("Product Version is " + version.getFull());
			pluginService = new PluginServiceImpl(clientService.getServerURL(), version.getCommit(),
				clientService.getService(RemotePluginDownloadService.class));
		}
		this.pluginService = pluginService;
		try
		{
			pluginService.registerPlugins();
		}
		catch( JpfException e )
		{
			throw new RuntimeException(e);
		}
		session.enableKeepAlive(true);
	}

	@Deprecated
	public ClientService getClientService()
	{
		return clientService;
	}

	public static void displayError(Component parent, String titleKey, String messageKey, Throwable throwable)
	{
		displayErrorRaw(parent, CurrentLocale.get(titleKey), CurrentLocale.get(messageKey), throwable);
	}

	@Deprecated
	public static void displayError(Component parent, String messageGroup, Throwable throwable)
	{
		PropBagEx xml = Messages.getInstance().getError(messageGroup);
		if( xml == null )
		{
			xml = Messages.getInstance().getError("unknown"); //$NON-NLS-1$
		}
		String title = xml.getNode("title"); //$NON-NLS-1$
		String message = xml.getNode("message"); //$NON-NLS-1$
		String thrownMsg = throwable.getMessage();
		if( !Check.isEmpty(thrownMsg) )
		{
			// Most likely thrown message is prefixed with a ':' separated chain
			// of exception types, which is just clutter for the purpose of a
			// quick display of brief message.
			if( thrownMsg.contains(COLON) )
			{
				thrownMsg = thrownMsg.substring(thrownMsg.lastIndexOf(COLON) + 1);
			}
			message += "\n\n" + thrownMsg; //$NON-NLS-1$
		}
		message = message.replaceAll("\\\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		displayErrorRaw(parent, title, message, throwable);
	}

	public static void displayErrorRaw(Component parent, String title, String message, Throwable throwable)
	{
		String version = instance().getVersion().getFull();
		ExceptionDialog ed;

		parent = SwingUtilities.getWindowAncestor(parent);
		if( parent instanceof Dialog )
		{
			ed = new ExceptionDialog((Dialog) parent, title, message, version, throwable);
		}
		else
		{
			ed = new ExceptionDialog((Frame) parent, title, message, version, throwable);
		}

		ed.setTitle(CurrentLocale.get("com.tle.admin.driver.title")); //$NON-NLS-1$
		ed.setVisible(true);
	}

	public static void displayInformation(Component parent, String message)
	{
		JOptionPane.showMessageDialog(parent, message,
			CurrentLocale.get("com.tle.admin.driver.info"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
	}

	// ///////// SOAPED //////////////////////////////////////////////

	public Version getVersion()
	{
		return version;
	}

	public String getInstitutionName()
	{
		return institutionName;
	}

	public String getLoggedInUserUUID()
	{
		return loggedInUserID;
	}

	public PluginServiceImpl getPluginService()
	{
		return pluginService;
	}

	public ControlRepository getControlRepository()
	{
		if( controlRepository == null )
		{
			controlRepository = new ControlRepositoryImpl(pluginService);
		}
		return controlRepository;
	}
}
