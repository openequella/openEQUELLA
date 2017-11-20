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

package com.tle.web.htmleditor.tinymce.addon.tle.migration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKey;
import org.hibernate.classic.Session;

import com.tle.common.i18n.KeyString;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.web.htmleditor.tinymce.addon.tle.TinyMceAddonConstants;

/**
 * @author Aaron
 */
@SuppressWarnings({"deprecation", "nls"})
@Bind
@Singleton
public class ModifyEmbeddingTemplatesDatabaseMigration extends AbstractHibernateDataMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(ModifyEmbeddingTemplatesDatabaseMigration.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(new KeyString(KEY_PREFIX + "mimetemplate.migration"));
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		@SuppressWarnings("unchecked")
		final List<FakeMimeEntry> entryList = session.createQuery("FROM MimeEntry WHERE type IN :types")
			.setParameterList("types", ModifyEmbeddingTemplatesXmlMigration.getMigratableMimeTypes()).list();
		for( FakeMimeEntry entry : entryList )
		{
			final String oldTemplate = ModifyEmbeddingTemplatesXmlMigration.getOldTemplate(entry.type);
			final String currentTemplate = entry.attributes.get(TinyMceAddonConstants.MIME_TEMPLATE_KEY);
			// Only upgrade if it hasn't been changed
			if( currentTemplate == null || oldTemplate.equals(currentTemplate) )
			{
				final String newTemplate = ModifyEmbeddingTemplatesXmlMigration.getNewTemplate(entry.type);
				entry.attributes.put(TinyMceAddonConstants.MIME_TEMPLATE_KEY, newTemplate);
				session.save(entry);
			}
			result.incrementStatus();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session.createQuery("SELECT COUNT(*) FROM MimeEntry WHERE type IN :types").setParameterList(
			"types", ModifyEmbeddingTemplatesXmlMigration.getMigratableMimeTypes()));
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeMimeEntry.class};
	}

	@Entity(name = "MimeEntry")
	@AccessType("field")
	public static class FakeMimeEntry
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Column(length = 100, nullable = false)
		String type;

		// Hibernate has no trouble mapping this to the existing Table (with Column Lob),
		// whereas Oracle finds an excuse to throw a<br>
		// java.sql.SQLException: Invalid column type: getCLOB not implemented for class oracle.jdbc.driver.T4CVarcharAccessor
		// if we explicitly specify @Lob.
		@CollectionOfElements
		@Column(nullable = false)
		@MapKey(columns = {@Column(length = 100, nullable = false)})
		Map<String, String> attributes = new HashMap<String, String>();
	}
}
