package com.tle.core.htmleditor.migration.v61;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class CreateHtmlEditorPluginSchema extends AbstractCreateMigration
{
	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		Set<String> tables = new HashSet<String>();
		tables.add("html_editor_plugin");
		return new TablesOnlyFilter(tables.toArray(new String[tables.size()]));
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{HtmlEditorPlugin.class, BaseEntity.class, BaseEntity.Attribute.class, LanguageBundle.class,
				Institution.class, LanguageString.class};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.htmleditor.migration.htmleditorschema.title", "");
	}
}
