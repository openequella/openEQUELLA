/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.admin.search.searchset.virtualisation;

import java.awt.GridLayout;

import javax.swing.JLabel;

import com.tle.admin.gui.common.DynamicChoicePanel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.searchset.SearchSet;
import com.tle.core.plugins.AbstractPluginService;

@SuppressWarnings("nls")
public class ContributedValuesVirtualiserConfigPanel extends DynamicChoicePanel<SearchSet>
{
	private String KEY_PFX = AbstractPluginService.getMyPluginId(getClass()) + ".";

	protected String getString(String key)
	{
		return CurrentLocale.get(getKey(key));
	}

	protected String getKey(String key)
	{
		return KEY_PFX+key;
	}

	public ContributedValuesVirtualiserConfigPanel()
	{
		super(new GridLayout(1, 1));
		add(new JLabel("<html>"
			+ getString("searchset.virtualisation.contributedvalues.text")));
	}

	@Override
	public void load(SearchSet searchSet)
	{
		// Nothing to load
	}

	@Override
	public void save(SearchSet searchSet)
	{
		// Nothing to save
	}

	@Override
	public void removeSavedState(SearchSet searchSet)
	{
		// Nothing to remove
	}
}
