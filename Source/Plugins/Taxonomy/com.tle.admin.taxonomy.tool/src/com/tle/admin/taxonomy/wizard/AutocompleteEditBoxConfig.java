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

package com.tle.admin.taxonomy.wizard;

import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.tle.admin.gui.common.DynamicChoicePanel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.wizard.AutocompleteEditBoxConstants;
import com.tle.common.taxonomy.wizard.TermSelectorControl;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class AutocompleteEditBoxConfig extends DynamicChoicePanel<TermSelectorControl>
{
	private final JCheckBox reloadOnTermSelection;

	public AutocompleteEditBoxConfig()
	{
		super(new MigLayout("wrap 1, insets 0"));

		reloadOnTermSelection = new JCheckBox(s("reloadOnTermSelection"), true);

		add(new JLabel("<html>" + s("description")));
		add(reloadOnTermSelection);

		changeDetector.watch(reloadOnTermSelection);
	}

	@Override
	public void load(TermSelectorControl state)
	{
		reloadOnTermSelection.setSelected(state
			.getBooleanAttribute(AutocompleteEditBoxConstants.RELOAD_PAGE_ON_SELECTION));
	}

	@Override
	public void removeSavedState(TermSelectorControl state)
	{
		Map<Object, Object> as = state.getAttributes();
		as.remove(AutocompleteEditBoxConstants.RELOAD_PAGE_ON_SELECTION);
	}

	@Override
	public void save(TermSelectorControl state)
	{
		Map<Object, Object> as = state.getAttributes();
		as.put(AutocompleteEditBoxConstants.RELOAD_PAGE_ON_SELECTION, reloadOnTermSelection.isSelected());
	}

	private static String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.taxonomy.tool.wizard.autocompleteEditBox." + keyPart);
	}
}
