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

import java.awt.Rectangle;

import javax.swing.JCheckBox;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.EditorException;
import com.tle.admin.hierarchy.TopicEditor.AbstractTopicEditorTab;
import com.tle.admin.search.searchset.SearchSetFilter;
import com.tle.beans.hierarchy.HierarchyPack;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.client.EntityCache;
import com.tle.common.hierarchy.SearchSetAdapter;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class FilteringTab extends AbstractTopicEditorTab
{
	private final EntityCache cache;
	private final ClientService clientService;

	private JCheckBox showResults;
	private SearchSetFilter filter;

	public FilteringTab(EntityCache cache, ClientService clientService)
	{
		this.cache = cache;
		this.clientService = clientService;
	}

	@Override
	public void setup(ChangeDetector changeDetector)
	{
		showResults = new JCheckBox(CurrentLocale.get("com.tle.admin.hierarchy.tool.filteringtab.display"), true); //$NON-NLS-1$
		filter = new SearchSetFilter(cache, clientService);

		final int[] rows = {TableLayout.PREFERRED, TableLayout.FILL,};
		final int[] cols = {TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));

		add(showResults, new Rectangle(0, 0, 1, 1));
		add(filter, new Rectangle(0, 1, 1, 1));

		changeDetector.watch(showResults);
		changeDetector.watch(filter);
	}

	@Override
	public void load(HierarchyPack pack)
	{
		HierarchyTopic topic = pack.getTopic();
		showResults.setSelected(topic.isShowResults());
		filter.load(new SearchSetAdapter(topic));
	}

	@Override
	public void save(HierarchyPack pack)
	{
		HierarchyTopic topic = pack.getTopic();
		topic.setShowResults(showResults.isSelected());
		filter.save(new SearchSetAdapter(topic));
	}

	@Override
	public void validation() throws EditorException
	{
		// nothing to validate
	}
}
