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
