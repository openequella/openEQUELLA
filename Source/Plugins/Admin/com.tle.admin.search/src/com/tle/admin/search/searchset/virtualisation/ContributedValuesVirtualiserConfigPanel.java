package com.tle.admin.search.searchset.virtualisation;

import java.awt.GridLayout;

import javax.swing.JLabel;

import com.tle.admin.gui.common.DynamicChoicePanel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.search.searchset.SearchSet;

@SuppressWarnings("nls")
public class ContributedValuesVirtualiserConfigPanel extends DynamicChoicePanel<SearchSet>
{
	public ContributedValuesVirtualiserConfigPanel()
	{
		super(new GridLayout(1, 1));
		add(new JLabel("<html>"
			+ CurrentLocale.get("com.tle.admin.search.searchset.virtualisation.contributedvalues.text")));
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
