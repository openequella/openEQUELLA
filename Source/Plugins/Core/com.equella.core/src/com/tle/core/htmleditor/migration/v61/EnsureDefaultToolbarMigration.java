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

package com.tle.core.htmleditor.migration.v61;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
import com.tle.beans.ConfigurationProperty;
import com.tle.beans.ConfigurationProperty.PropertyKey;
import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class EnsureDefaultToolbarMigration extends AbstractHibernateDataMigration
{
	// As ripped from my DB
	public static final List<NameValue> DEFAULT_TOOLBAR = Lists.newArrayList();
	static
	{
		//@formatter:off
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.0", "bold"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.1", "italic"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.10", "formatselect"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.11", "hr"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.12", "removeformat"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.13", "visualaid"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.14", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.15", "sub"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.16", "sup"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.17", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.18", "charmap"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.19", "link"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.2", "underline"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.20", "unlink"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.21", "anchor"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.22", "image"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.3", "strikethrough"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.4", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.5", "justifyleft"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.6", "justifycenter"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.7", "justifyright"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.8", "justifyfull"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.0.buttons.9", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.0", "code"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.1", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.10", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.11", "row_props"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.12", "cell_props"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.13", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.14", "row_before"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.15", "row_after"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.16", "delete_row"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.17", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.18", "col_before"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.19", "col_after"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.2", "undo"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.20", "delete_col"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.21", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.22", "split_cells"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.23", "merge_cells"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.24", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.25", "forecolorpicker"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.26", "backcolorpicker"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.3", "redo"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.4", "cut"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.5", "copy"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.6", "paste"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.7", "selectall"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.8", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.1.buttons.9", "table"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.0", "bullist"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.1", "numlist"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.10", "fontselect"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.11", "fontsizeselect"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.12", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.13", "spellchecker"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.2", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.3", "outdent"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.4", "indent"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.5", "|"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.6", "tle_fileuploader"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.7", "tle_reslinker"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.8", "tle_scrapbookpicker"));
		DEFAULT_TOOLBAR.add(new NameValue("htmleditor.toolbar.rows.2.buttons.9", "|"));
		//@formatter:on
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.htmleditor.migration.defaulttoolbar.title");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		final List<FakeInstitution> institutions = session.createQuery("FROM Institution").list();
		for( FakeInstitution institution : institutions )
		{
			for( NameValue nv : DEFAULT_TOOLBAR )
			{
				final ConfigurationProperty cfg = new ConfigurationProperty();
				cfg.setKey(new PropertyKey(institution.id, nv.getName()));
				cfg.setValue(nv.getValue());
				session.save(cfg);
				result.incrementStatus();
			}
		}
		session.flush();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 61;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{ConfigurationProperty.class, PropertyKey.class, FakeInstitution.class};
	}

	@Entity(name = "Institution")
	@AccessType("field")
	public static class FakeInstitution
	{
		@Id
		long id;
	}
}
