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

package com.tle.core.institution.migration.v41;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;
import org.hibernate.classic.Session;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.beans.entity.itemdef.DisplayNode;
import com.tle.beans.entity.itemdef.DisplayTemplate;
import com.tle.beans.entity.itemdef.DisplayTemplate.DisplayType;
import com.tle.beans.entity.itemdef.SummaryDisplayTemplate;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.xml.service.XmlService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DisplayTemplateMigration extends AbstractHibernateSchemaMigration
{
	private static final String TABLE_NAME = "itemdef_blobs";
	private static final String OLD_COLUMN = "item_summary_template";
	private static final String NEW_SETTING_COLUMN = "item_summary_sections";

	@Inject
	private XmlService xmlService;

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.summarydisplaymigrate.title");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{ItemdefBlobs.class, DisplayTemplate.class, SummaryDisplayTemplate.class, DisplayNode.class,
				LanguageString.class, LanguageBundle.class};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = new ArrayList<String>();
		sql.addAll(helper.getAddColumnsSQL(TABLE_NAME, NEW_SETTING_COLUMN));
		sql.addAll(helper.getAddIndexesAndConstraintsForColumns(TABLE_NAME, NEW_SETTING_COLUMN));
		return sql;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> sql = new ArrayList<String>();
		sql.addAll(helper.getDropColumnSQL(TABLE_NAME, OLD_COLUMN));
		return sql;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		Query cquery = session.createQuery("select count(*) from ItemdefBlobs");
		return ((Number) cquery.uniqueResult()).intValue();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		Query query = session.createQuery("from ItemdefBlobs");
		List<ItemdefBlobs> list = query.list();
		for( ItemdefBlobs blob : list )
		{
			DisplayTemplate dispTemplate = blob.itemSummaryTemplate;
			if( dispTemplate == null )
			{
				dispTemplate = new DisplayTemplate();
			}

			blob.itemSummarySections = convertToNew(dispTemplate);
			result.incrementStatus();
			session.save(blob);
			session.flush();
		}
	}

	public SummaryDisplayTemplate convertToNew(DisplayTemplate dispTemplate)
	{
		DisplayType type = dispTemplate.getType();
		if( type == null )
		{
			type = DisplayType.DEFAULT;
		}

		final List<SummarySectionsConfig> nodes;

		if( type.equals(DisplayType.XSLT) )
		{
			SummarySectionsConfig node = new SummarySectionsConfig("xsltSection");
			node.setConfiguration(dispTemplate.getXsltFilename());
			// English strings are OK here - titles are not I18Nable
			node.setTitle("XSLT");

			nodes = new ArrayList<SummarySectionsConfig>();
			nodes.add(node);
		}
		else
		{
			nodes = SummarySectionsConfig.createDefaultConfigs();

			if( type.equals(DisplayType.DISPLAY_NODES) )
			{
				SummarySectionsConfig node = new SummarySectionsConfig("displayNodes");
				node.setConfiguration(xmlService.serialiseToXml(dispTemplate.getDisplayNodes()));
				// English strings are OK here - titles are not I18Nable
				node.setTitle("Extra Metadata");

				nodes.add(1, node);
			}
		}

		SummaryDisplayTemplate summaryTemplate = new SummaryDisplayTemplate();
		summaryTemplate.setConfigList(nodes);
		return summaryTemplate;
	}

	@Entity(name = "ItemdefBlobs")
	@AccessType("field")
	public static class ItemdefBlobs
	{
		@Id
		long id;

		@Type(type = "xstream_immutable")
		DisplayTemplate itemSummaryTemplate;
		@Type(type = "xstream_immutable")
		SummaryDisplayTemplate itemSummarySections;
	}
}
