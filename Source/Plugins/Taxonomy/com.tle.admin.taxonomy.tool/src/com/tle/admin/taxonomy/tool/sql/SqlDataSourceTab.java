package com.tle.admin.taxonomy.tool.sql;

import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_DATA_CLASS;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_JDBC_URL;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_PASSWORD;
import static com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.SQL_USERNAME;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.dytech.gui.Changeable;
import com.tle.admin.remotesqlquerying.QueryState;
import com.tle.admin.remotesqlquerying.SqlConnectionAndQueryPanel;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.datasource.sql.SqlTaxonomyDataSourceConstants.Query;

@SuppressWarnings("nls")
public class SqlDataSourceTab extends JPanel implements Changeable
{
	private final SqlConnectionAndQueryPanel scaqp;

	private List<QueryState> queries;

	public SqlDataSourceTab(ClientService clientService, boolean readonly)
	{
		super(new GridLayout(1, 1));

		queries = new ArrayList<QueryState>();
		for( Query q : Query.values() )
		{
			String key = q.toString();
			queries.add(new QueryState(key, CurrentLocale.get("com.tle.admin.taxonomy.tool.sql.tab.queryname." + key),
				"<html>" + CurrentLocale.get("com.tle.admin.taxonomy.tool.sql.tab.querydesc." + key)));
		}

		scaqp = new SqlConnectionAndQueryPanel(clientService);
		scaqp.load(null, null, null, null, queries);

		add(scaqp);

		if( readonly )
		{
			scaqp.setEnabled(false);
		}
	}

	@Override
	public void clearChanges()
	{
		scaqp.clearChanges();
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return scaqp.hasDetectedChanges();
	}

	public void load(Taxonomy state)
	{
		for( QueryState qs : queries )
		{
			qs.setSql(state.getAttribute(qs.getKey()));
		}

		scaqp.load(state.getAttribute(SQL_DATA_CLASS), state.getAttribute(SQL_JDBC_URL),
			state.getAttribute(SQL_USERNAME), state.getAttribute(SQL_PASSWORD), queries);
	}

	public void save(Taxonomy state)
	{
		state.setAttribute(SQL_DATA_CLASS, scaqp.getDriverClass());
		state.setAttribute(SQL_JDBC_URL, scaqp.getJdbcUrl());
		state.setAttribute(SQL_USERNAME, scaqp.getUsername());
		state.setAttribute(SQL_PASSWORD, scaqp.getPassword());

		for( QueryState q : scaqp.getQueries() )
		{
			state.setAttribute(q.getKey(), q.getSql());
		}
	}
}
