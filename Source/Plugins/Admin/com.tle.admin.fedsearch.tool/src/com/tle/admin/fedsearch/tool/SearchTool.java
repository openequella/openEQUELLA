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

package com.tle.admin.fedsearch.tool;

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

import com.tle.admin.fedsearch.SearchManagement;
import com.tle.admin.fedsearch.SearchPlugin;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.FederatedSearch;
import com.tle.common.EntityPack;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteAbstractEntityService;
import com.tle.core.remoting.RemoteFederatedSearchService;

public class SearchTool extends BaseEntityTool<FederatedSearch>
{
	final SortedMap<String, String> plugins = new TreeMap<String, String>();
	final Map<String, Extension> tools = new HashMap<String, Extension>();

	public SearchTool() throws Exception
	{
		super(FederatedSearch.class, RemoteFederatedSearchService.ENTITY_TYPE);
	}

	@Override
	public void setup(Set<String> grantedPrivileges, String toolName)
	{
		super.setup(grantedPrivileges, toolName);

		Collection<Extension> extensions = driver.getPluginService().getConnectedExtensions(
			"com.tle.admin.fedsearch.tool", "configUI");
		for( Extension extension : extensions )
		{
			String type = extension.getParameter("type").valueAsString();
			String name = extension.getParameter("name").valueAsString();
			plugins.put(name, type);
			tools.put(type, extension);
		}
	}

	@Override
	protected RemoteAbstractEntityService<FederatedSearch> getService(ClientService client)
	{
		return client.getService(RemoteFederatedSearchService.class);
	}

	public SearchPlugin getToolInstance(String type)
	{
		Extension extension = tools.get(type);
		return (SearchPlugin) driver.getPluginService().getBean(extension.getDeclaringPluginDescriptor(),
			extension.getParameter("class").valueAsString());
	}

	@Override
	protected EntityPack<FederatedSearch> create()
	{
		PluginDialog plugin = new PluginDialog();
		JOptionPane.showMessageDialog(parentFrame, plugin,
			CurrentLocale.get("com.tle.admin.gui.searchtool.createdialog.title"), JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$
		String tool = plugin.getSelectedTool();
		EntityPack<FederatedSearch> pack = null;
		if( tool != null )
		{
			String search = plugins.get(tool);
			pack = super.create();
			pack.getEntity().setType(search);
		}
		return pack;
	}

	@Override
	protected SearchManagement createEditor(boolean readonly)
	{
		return new SearchManagement(this, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.gui.searchtool.name");
	}

	@Override
	protected String getErrorPath()
	{
		return "searching";
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
