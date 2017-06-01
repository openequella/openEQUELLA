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

package com.tle.admin.taxonomy.tool.internal;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.tle.admin.taxonomy.tool.DataSourceChoice;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.TaxonomyConstants;

@SuppressWarnings("nls")
public class InternalDataSourceChoice extends DataSourceChoice
{
	private InternalDataSourceTab tab;
	private JCheckBox allowNewTerms;

	public InternalDataSourceChoice()
	{
		super(new MigLayout("wrap 1"));

		allowNewTerms = new JCheckBox(CurrentLocale.get("com.tle.admin.taxonomy.tool.internal.allownewterms"));

		add(new JLabel("<html>" + CurrentLocale.get("com.tle.admin.taxonomy.tool.internal.choicedescription")));
		add(allowNewTerms);

		changeDetector.watch(allowNewTerms);
	}

	@Override
	public void choiceSelected()
	{
		ensureTab();
		addTab(CurrentLocale.get("com.tle.admin.taxonomy.tool.internal.tab.title"), tab);
	}

	@Override
	public void load(Taxonomy state)
	{
		ensureTab();
		tab.load(state);
		allowNewTerms.setSelected(state.getAttribute(TaxonomyConstants.TERM_ALLOW_ADDITION, false));
	}

	@Override
	public void save(Taxonomy state)
	{
		state.setAttribute(TaxonomyConstants.TERM_ALLOW_ADDITION, Boolean.toString(allowNewTerms.isSelected()));
		tab.save(state);
	}

	@Override
	public void afterSave()
	{
		tab.afterSave();
	}

	@Override
	public void removeSavedState(Taxonomy state)
	{
		// TODO?
	}

	private void ensureTab()
	{
		if( tab == null )
		{
			tab = new InternalDataSourceTab(getClientService(), getPluginService());
		}
	}
}
