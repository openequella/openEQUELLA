package com.tle.admin.hierarchy;

import java.awt.GridLayout;

import com.dytech.gui.ChangeDetector;
import com.tle.admin.gui.EditorException;
import com.tle.admin.hierarchy.TopicEditor.AbstractTopicEditorTab;
import com.tle.admin.search.searchset.SearchSetInheritance;
import com.tle.beans.hierarchy.HierarchyPack;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.client.EntityCache;
import com.tle.common.hierarchy.SearchSetAdapter;

/**
 * @author Nicholas Read
 */
public class InheritanceTab extends AbstractTopicEditorTab
{
	private final ClientService clientService;
	private final EntityCache cache;

	private SearchSetInheritance inheritance;

	public InheritanceTab(EntityCache cache, ClientService clientService)
	{
		this.cache = cache;
		this.clientService = clientService;
	}

	@Override
	public void setup(ChangeDetector changeDetector)
	{
		inheritance = new SearchSetInheritance(cache, clientService);

		setLayout(new GridLayout(1, 1));
		add(inheritance);

		changeDetector.watch(inheritance);
	}

	@Override
	public void load(HierarchyPack pack)
	{
		inheritance.load(new SearchSetAdapter(pack.getTopic()), pack.getInheritedSchemas(),
			pack.getInheritedItemDefinitions());
	}

	@Override
	public void save(HierarchyPack pack)
	{
		inheritance.save(new SearchSetAdapter(pack.getTopic()));
	}

	@Override
	public void validation() throws EditorException
	{
		// nothing to validate
	}
}
