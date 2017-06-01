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
