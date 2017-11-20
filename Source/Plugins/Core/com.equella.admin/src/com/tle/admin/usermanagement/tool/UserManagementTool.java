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

package com.tle.admin.usermanagement.tool;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.PluginServiceImpl;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.admin.plugin.PluginDialog;
import com.tle.admin.usermanagement.AbstractUMPlugin;
import com.tle.admin.usermanagement.UMPConfig;
import com.tle.admin.usermanagement.UMWConfig;
import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ExtensionParamComparator;
import com.tle.core.remoting.RemoteUserService;

public class UserManagementTool extends AdminToolSelect
{
	protected static final Log LOGGER = LogFactory.getLog(UserManagementTool.class);
	private Collection<UMWConfig> wrappers;
	private String toolName;
	private RemoteUserService userService;

	public UserManagementTool()
	{
		super();
	}

	@Override
	public void setup(Set<String> grantedPrivileges, String toolName)
	{
		this.toolName = toolName;
		wrappers = new ArrayList<UMWConfig>();

		ClientService clientService = driver.getClientService();
		PluginServiceImpl pluginService = driver.getPluginService();
		userService = clientService.getService(RemoteUserService.class);

		PluginTracker tracker = new PluginTracker(pluginService, "com.tle.admin.usermanagement.tool", "configUI", null, //$NON-NLS-1$ //$NON-NLS-2$
			new ExtensionParamComparator("displayorder")); //$NON-NLS-1$
		Collection<Extension> extensions = tracker.getExtensions();
		for( Extension extension : extensions )
		{
			String settingsClass = extension.getParameter("settingsClass").valueAsString(); //$NON-NLS-1$

			Parameter param = extension.getParameter("class"); //$NON-NLS-1$
			String className = param != null ? param.valueAsString() : null;
			param = extension.getParameter("width"); //$NON-NLS-1$
			int width = param != null ? param.valueAsNumber().intValue() : 0;
			param = extension.getParameter("height"); //$NON-NLS-1$
			int height = param != null ? param.valueAsNumber().intValue() : 0;
			String name = CurrentLocale.get(extension.getParameter("name").valueAsString()); //$NON-NLS-1$

			UMWConfig umw = new UMWConfig(className, settingsClass, name, width, height, extension);
			wrappers.add(umw);

			pluginService.ensureActivated(extension.getDeclaringPluginDescriptor());
			UserManagementSettings xml = userService.getPluginConfig(settingsClass);
			if( xml != null )
			{
				umw.setEnabled(xml.isEnabled());
			}
			else
			{
				umw.setVisible(false);
			}
		}

		super.setup(grantedPrivileges, toolName);
	}

	@Override
	protected void fillLists()
	{
		for( UMWConfig umw : wrappers )
		{
			if( umw.isVisible() )
			{
				addWrapperElement(umw);
			}
		}
		wrapperList.updateUI();
	}

	@Override
	public void onConfigure(Object selected, final boolean select)
	{
		final UMPConfig plugin = (UMPConfig) selected;

		final GlassSwingWorker<Object> worker = new GlassSwingWorker<Object>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public Object construct() throws Exception
			{
				if( plugin == null )
				{
					return null;
				}
				if( Check.isEmpty(plugin.getPluginClass()) )
				{
					return CurrentLocale.get("com.tle.admin.gui.usermanagementtool.noconfiguration");
				}

				PluginServiceImpl pluginService = driver.getPluginService();
				GeneralPlugin<UserManagementSettings> umplugin = (GeneralPlugin<UserManagementSettings>) pluginService
					.getBean(plugin.getExtension().getDeclaringPluginDescriptor(), plugin.getPluginClass());
				return createDialog(parentFrame, toolName, plugin, umplugin);
			}

			@SuppressWarnings("unchecked")
			@Override
			public void finished()
			{
				Object o = get();
				if( o != null )
				{
					if( o instanceof PluginDialog )
					{
						PluginDialog window = (PluginDialog) o;
						window.setModal(true);
						window.setVisible(true);
					}
					else
					{
						Driver.displayInformation(parentFrame, o.toString());
					}
				}
				else
				{
					Driver.displayInformation(parentFrame,
						CurrentLocale.get("com.tle.admin.gui.usermanagementtool.noplugin"));
				}
			}

			@Override
			public void exception()
			{
				Exception ex = getException();
				LOGGER.error("Problem creating UMP '" + (plugin == null ? plugin : plugin.getClass()) + '\'', ex);
				Driver.displayError(parentFrame, "user.management/loading", ex);
			}
		};

		worker.setComponent(parentFrame);
		worker.start();
	}

	public PluginDialog<UserManagementSettings, UMPConfig> createDialog(Frame frame, String title, UMPConfig setting,
		GeneralPlugin<UserManagementSettings> gplugin)
	{
		AbstractUMPlugin plugin = new AbstractUMPlugin(frame, title, setting, gplugin, userService);
		plugin.setup();
		return plugin;
	}
}
