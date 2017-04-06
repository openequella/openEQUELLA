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
