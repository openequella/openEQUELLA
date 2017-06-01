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

package com.tle.admin.harvester.tool;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.java.plugin.registry.Extension;

import com.tle.admin.harvester.standard.HarvesterPlugin;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.common.EntityPack;
import com.tle.common.applet.client.ClientService;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.harvester.RemoteHarvesterProfileService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteAbstractEntityService;

public class HarvesterProfileTool extends BaseEntityTool<HarvesterProfile>
{

	final SortedMap<String, String> plugins = new TreeMap<String, String>();
	final Map<String, Extension> tools = new HashMap<String, Extension>();

	public HarvesterProfileTool() throws Exception
	{
		super(HarvesterProfile.class, RemoteHarvesterProfileService.ENTITY_TYPE);
	}

	@Override
	protected RemoteAbstractEntityService<HarvesterProfile> getService(ClientService client)
	{
		return client.getService(RemoteHarvesterProfileService.class);
	}

	@Override
	protected HarvesterProfileEditor createEditor(boolean readonly)
	{
		return new HarvesterProfileEditor(this, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.harvester.tool.entityname"); //$NON-NLS-1$
	}

	@Override
	protected String getErrorPath()
	{
		return "harvester"; //$NON-NLS-1$
	}

	@Override
	public void setup(Set<String> grantedPrivileges, String toolName)
	{
		super.setup(grantedPrivileges, toolName);

		Collection<Extension> extensions = driver.getPluginService().getConnectedExtensions(
			"com.tle.admin.harvester.tool", "harvesterType"); //$NON-NLS-1$ //$NON-NLS-2$
		for( Extension extension : extensions )
		{

			String type = extension.getParameter("type").valueAsString(); //$NON-NLS-1$
			String name = extension.getParameter("name").valueAsString(); //$NON-NLS-1$
			plugins.put(name, type);
			tools.put(type, extension);
		}
	}

	public HarvesterPlugin getToolInstance(String type)
	{
		Extension extension = tools.get(type);
		return (HarvesterPlugin) driver.getPluginService().getBean(extension.getDeclaringPluginDescriptor(),
			extension.getParameter("class").valueAsString()); //$NON-NLS-1$
	}

	@Override
	protected EntityPack<HarvesterProfile> create()
	{
		PluginDialog plugin = new PluginDialog();
		JOptionPane.showMessageDialog(parentFrame, plugin,
			CurrentLocale.get("com.tle.admin.harvester.tool.createdialog.title"), JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$
		String tool = plugin.getSelectedTool();
		EntityPack<HarvesterProfile> pack = null;
		if( tool != null )
		{
			String harvester = plugins.get(tool);
			pack = super.create();
			pack.getEntity().setType(harvester);
		}
		return pack;
	}

	private class PluginDialog extends JPanel
	{
		private static final long serialVersionUID = 1L;
		private JList list;
		private DefaultListModel listModel;

		public PluginDialog()
		{
			listModel = new DefaultListModel();

			list = new JList();
			list.setModel(listModel);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			for( String name : plugins.keySet() )
			{
				listModel.addElement(name);
			}

			setLayout(new BorderLayout());
			add(new JScrollPane(list));
		}

		public String getSelectedTool()
		{
			return (String) list.getSelectedValue();
		}
	}
}
