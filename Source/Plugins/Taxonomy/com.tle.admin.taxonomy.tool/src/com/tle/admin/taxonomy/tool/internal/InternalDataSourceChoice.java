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
