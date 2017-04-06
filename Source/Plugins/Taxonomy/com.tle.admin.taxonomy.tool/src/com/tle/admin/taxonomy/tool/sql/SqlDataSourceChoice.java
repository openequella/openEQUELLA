package com.tle.admin.taxonomy.tool.sql;

import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_DATA_CLASS;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_JDBC_URL;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_PASSWORD;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_USERNAME;

import java.awt.GridLayout;

import javax.swing.JLabel;

import com.tle.admin.taxonomy.tool.DataSourceChoice;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.Taxonomy;

@SuppressWarnings("nls")
public class SqlDataSourceChoice extends DataSourceChoice
{
	private SqlDataSourceTab tab;

	public SqlDataSourceChoice()
	{
		super(new GridLayout(1, 1));
		add(new JLabel(CurrentLocale.get("com.tle.admin.taxonomy.tool.sql.choicedescription")));
	}

	@Override
	public void choiceSelected()
	{
		if( tab == null )
		{
			tab = new SqlDataSourceTab(getClientService(), isReadonly());
		}
		addTab(CurrentLocale.get("com.tle.admin.taxonomy.tool.sql.tab.title"), tab);
	}

	@Override
	public void load(Taxonomy state)
	{
		// choiceSelected *should* have been called
		// before any invocations of this method.
		tab.load(state);
	}

	@Override
	public void save(Taxonomy state)
	{
		tab.save(state);
	}

	@Override
	public void removeSavedState(Taxonomy state)
	{
		state.removeAttribute(SQL_DATA_CLASS);
		state.removeAttribute(SQL_JDBC_URL);
		state.removeAttribute(SQL_USERNAME);
		state.removeAttribute(SQL_PASSWORD);
	}
}
