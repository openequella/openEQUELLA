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

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.beans.Institution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class ItemXmlTwoWayMigration extends AbstractHibernateSchemaMigration
{
	private static final int BATCH_SIZE = 1000;
	private static final String ITEMXML_TABLE_NAME = "item_xml";
	private static final String ITEM_TABLE_NAME = "item";

	// old
	private static final String XML_COLUMN = "xml";
	private static final String ITEM_UUID_COLUMN = "item_uuid";
	private static final String ITEM_VERSION_COLUMN = "item_version";
	private static final String ITEM_INSTITUTION_COLUMN = "institution_id";

	// new
	private static final String ITEMXML_ID_COLUMN = "item_xml_id";

	private static final Logger LOGGER = Logger.getLogger(ItemXmlTwoWayMigration.class);

	private boolean forgiving = false;

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.itemxmlkeymigrate.title");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{Item.class, ItemXml.class, Institution.class};
	}

	@Override
	protected String getDataKey()
	{
		return "com.tle.core.entity.services.itemxmlkeymigrate.datastatus";
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = new ArrayList<String>();
		sql.addAll(helper.getAddColumnsSQL(ITEM_TABLE_NAME, ITEMXML_ID_COLUMN));

		return sql;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> sql = new ArrayList<String>();
		sql.addAll(helper.getDropColumnSQL(ITEMXML_TABLE_NAME, ITEM_UUID_COLUMN));
		sql.addAll(helper.getDropColumnSQL(ITEMXML_TABLE_NAME, ITEM_VERSION_COLUMN));
		sql.addAll(helper.getDropColumnSQL(ITEMXML_TABLE_NAME, ITEM_INSTITUTION_COLUMN));

		// Not nulls
		sql.addAll(helper.getAddNotNullSQL(ITEM_TABLE_NAME, ITEMXML_ID_COLUMN));
		sql.addAll(helper.getAddNotNullSQL(ITEMXML_TABLE_NAME, XML_COLUMN));
		sql.addAll(helper.getAddIndexesAndConstraintsForColumns(ITEM_TABLE_NAME, ITEMXML_ID_COLUMN));
		return sql;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		Query cquery = session.createQuery("select count(*) from ItemXml");
		return ((Number) cquery.uniqueResult()).intValue();
	}

	/**
	 * Note: this method does a lot of bad data checking since the database
	 * constraints were previously very loose. We may need to decide if
	 * FORGIVING = true is the default if bad data appears to be very common.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		// !! Oracle complains if there is an alias on the subselect, whereas
		// SQL Server and Postgres need it
		String alias = "";
		if( helper.getExtDialect().requiresAliasOnSubselect() )
		{
			alias = " AS t";
		}

		final int duplicateCount = ((Number) session
			.createSQLQuery("SELECT COUNT(*) FROM (SELECT item_uuid FROM item_xml GROUP BY item_uuid,"
				+ " item_version, institution_id HAVING COUNT(*) > 1)" + alias)
			.uniqueResult()).intValue();
		if( duplicateCount > 0 )
		{
			throw new Exception(
				duplicateCount + " items were found with multiple XML objects.  Please contact EQUELLA Support.");
		}

		List<Object[]> list = session.createQuery(
			"select x.id, i.id FROM Item i, ItemXml x WHERE x.itemUuid = i.uuid AND x.itemVersion = i.version"
				+ " AND x.institution = i.institution")
			.list();
		session.clear();

		int i = 0;
		for( Object[] itemAndXml : list )
		{
			ItemXml itemXml = new ItemXml();
			itemXml.xml = "";
			Item item = new Item();
			itemXml.id = (Long) itemAndXml[0];
			item.id = (Long) itemAndXml[1];

			item.itemXml = itemXml;
			session.update(item);
			if( i % BATCH_SIZE == 0 )
			{
				session.flush();
				session.clear();
			}
			result.incrementStatus();
			i++;
		}
		session.flush();
		session.clear();

		// Add fake XML entries for items with no XML. These should never have
		// existed in the first place!
		if( forgiving )
		{
			Query itemsQuery = session.createQuery("FROM Item WHERE itemXml IS NULL");
			List<Item> items = itemsQuery.list();
			for( Item item : items )
			{
				LOGGER.warn("Item has no associated xml (item.id is " + item.id + ")");
				ItemXml itemXml = new ItemXml();
				itemXml.xml = "<xml/>";
				itemXml.itemUuid = "";
				itemXml.institution = item.institution;

				item.itemXml = itemXml;

				session.save(item);
				session.flush();
			}

			if( items.size() > 0 )
			{
				LOGGER.warn(items.size() + " items had no XML");
			}
		}
	}

	/**
	 * Spring settable via optional config (itemXmlMigrator.forgiving = true)
	 * 
	 * @param forgiving
	 */
	public void setForgiving(boolean forgiving)
	{
		this.forgiving = forgiving;
	}

	@Entity(name = "ItemXml")
	@AccessType("field")
	public static class ItemXml
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		@Lob
		String xml;

		// OLD
		@Index(name = "itemXmlItemUuid")
		String itemUuid;
		@Index(name = "itemXmlItemVersion")
		int itemVersion;
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "itemXmlInstIndex")
		Institution institution;
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class Item
	{
		@Id
		long id;

		@Column(updatable = false)
		String uuid;

		@Column(updatable = false)
		int version;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(updatable = false)
		Institution institution;

		// NEW
		@Index(name = "itemItemXmlIndex")
		@OneToOne(fetch = FetchType.LAZY)
		@JoinColumn(unique = true, nullable = false)
		ItemXml itemXml;
	}
}
