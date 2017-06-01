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

package com.tle.admin.hierarchy;

import java.awt.GridLayout;

import com.dytech.gui.ChangeDetector;
import com.tle.admin.gui.EditorException;
import com.tle.admin.hierarchy.TopicEditor.AbstractTopicEditorTab;
import com.tle.admin.search.searchset.virtualisation.VirtualisationEditor;
import com.tle.beans.hierarchy.HierarchyPack;
import com.tle.common.applet.client.ClientService;
import com.tle.common.hierarchy.SearchSetAdapter;
import com.tle.core.plugins.PluginService;

@SuppressWarnings("nls")
public class VirtualisationTab extends AbstractTopicEditorTab
{
	private final PluginService pluginService;
	private final ClientService clientService;

	private VirtualisationEditor editor;

	public VirtualisationTab(PluginService pluginService, ClientService clientService)
	{
		this.pluginService = pluginService;
		this.clientService = clientService;
	}

	@Override
	public void setup(ChangeDetector changeDetector)
	{
		editor = new VirtualisationEditor(pluginService, clientService,
			"com.tle.admin.hierarchy.tool.virtual.entityname", "com.tle.admin.hierarchy.tool.virtual.renameHelp");

		setLayout(new GridLayout(1, 1));
		add(editor);

		changeDetector.watch(editor);
	}

	@Override
	public void load(HierarchyPack pack)
	{
		editor.load(new SearchSetAdapter(pack.getTopic()));
	}

	@Override
	public void save(HierarchyPack pack)
	{
		editor.save(new SearchSetAdapter(pack.getTopic()));
	}

	@Override
	public void validation() throws EditorException
	{
		editor.validation();

	}

}
