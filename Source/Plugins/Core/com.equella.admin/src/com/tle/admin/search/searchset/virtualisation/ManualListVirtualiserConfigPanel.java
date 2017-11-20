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

package com.tle.admin.search.searchset.virtualisation;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import com.dytech.gui.JShuffleList;
import com.tle.admin.gui.common.DynamicChoicePanel;
import com.tle.common.Check;
import com.tle.common.search.searchset.SearchSet;
import com.tle.core.xstream.TLEXStream;

public class ManualListVirtualiserConfigPanel extends DynamicChoicePanel<SearchSet>
{
	private JShuffleList<String> list;

	public ManualListVirtualiserConfigPanel()
	{
		super(new GridLayout(1, 1));

		list = JShuffleList.newDefaultShuffleList(false);
		add(list);

		changeDetector.watch(list.getModel());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void load(SearchSet searchSet)
	{
		String v = searchSet.getAttribute(getId());
		if( !Check.isEmpty(v) )
		{
			list.addItems((List<String>) TLEXStream.instance().fromXML(v));
		}
	}

	@Override
	public void save(SearchSet searchSet)
	{
		searchSet.setAttribute(getId(), TLEXStream.instance().toXML(new ArrayList<String>(list.getItems())));
	}

	@Override
	public void removeSavedState(SearchSet searchSet)
	{
		searchSet.removeAttribute(getId());
	}
}
