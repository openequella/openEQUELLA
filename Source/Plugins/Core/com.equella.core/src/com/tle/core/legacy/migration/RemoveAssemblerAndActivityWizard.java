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

package com.tle.core.legacy.migration;

import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.filesystem.FileSystemHelper;
import com.tle.common.settings.annotation.Property;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.migration.MigrationStatusLog;
import com.tle.core.migration.MigrationStatusLog.LogType;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.services.FileSystemService;
import com.tle.core.xml.service.XmlService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveAssemblerAndActivityWizard extends AbstractHibernateSchemaMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(RemoveAssemblerAndActivityWizard.class)
		+ ".removeassembler.";

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private XmlService xmlService;

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "title", KEY_PREFIX + "description");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeActivityWizard.class, FakeActivityWizard.WebLink.class, FakeItemdefBlobs.class,
				FakeBaseEntity.class, FakeBaseEntity.Attribute.class, FakeLanguageBundle.class,
				FakeLanguageString.class, FakeItemDefinition.class, FakeItem.class, FakePowerSearch.class,
				FakeAttachment.class, FakeInstitution.class, FakeNavigationNode.class, FakeNavigationTab.class};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> sql = helper.getDropTableSql("activity_wizard_links", "activity_wizard");
		sql.addAll(helper.getDropColumnSQL("item_definition", "embedded_xslt"));
		sql.addAll(helper.getDropColumnSQL("item", "folder", "plan_metadata"));
		sql.addAll(helper.getDropColumnSQL("itemdef_blobs", "confirmation_template"));
		return sql;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 3 + count(session, "FROM ItemdefBlobs") + count(session, "FROM PowerSearch")
			+ count(session, "from Attachment where type = 'activity'");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		// Remove user preferences for the Assembler
		session.createSQLQuery("DELETE FROM user_preference WHERE preferenceid = 'assembler.preferences'")
			.executeUpdate();
		result.incrementStatus();

		// Remove system configuration dealing with the Assembler
		session.createSQLQuery("DELETE FROM configuration_property WHERE"
			+ " property LIKE 'assembler.%' OR property LIKE 'webct.%'"
			+ " OR property LIKE 'blackboard.%' OR property = 'soap.timeout'" + " OR property LIKE 'activity-wizard.%'")
			.executeUpdate();
		result.incrementStatus();

		// Remove any security privileges for Activity Wizards and the assembler
		session.createSQLQuery("DELETE FROM access_entry WHERE privilege LIKE '%_ACTIVITY_WIZARD'"
			+ " OR target_object = 'C:assemblerDrmReference' OR target_object = 'C:assesmblerFileTypes'"
			+ " OR target_object = 'C:assemblerLinks' OR target_object = 'C:lmsexport'"
			+ " OR target_object = 'C:taxonomies' OR target_object = 'C:webct'").executeUpdate();
		result.incrementStatus();

		// Remove wizard attributes that don't make sense
		final List<FakeItemdefBlobs> idbs = session.createQuery("FROM ItemdefBlobs").list();
		for( FakeItemdefBlobs idb : idbs )
		{
			removeWizardAttributes(idb, session, result);
		}

		final List<FakePowerSearch> powerSearches = session.createQuery("FROM PowerSearch").list();
		for( FakePowerSearch powerSearch : powerSearches )
		{
			removeWizardAttributes(powerSearch, session, result);
		}
		session.flush();
		session.clear();

		convertActivities(session, result);
	}

	@SuppressWarnings("unchecked")
	private void convertActivities(Session session, MigrationResult result) throws Exception
	{
		List<Long> itemIds = new ArrayList<Long>(
			session.createQuery("select distinct(a.item.id) from Attachment a where a.type = 'activity'").list());
		for( Long itemId : itemIds )
		{
			Query query = session.createQuery(
				"select a, a.item.institution.shortName from Attachment a where a.type = 'activity' and a.item.id = ? order by a.attindex");
			query.setParameter(0, itemId);
			int index = 0;
			List<Object[]> attList = query.list();
			for( Object[] attach : attList )
			{
				FakeAttachment activity = (FakeAttachment) attach[0];
				String shortName = (String) attach[1];
				FakeItem item = activity.item;
				if( Check.isEmpty(activity.value2) && Check.isEmpty(activity.url) )
				{
					activity.type = "html";
					//Assembler items would never have had collection bucket folders
					ItemFile itemFile = new ItemFile(item.uuid, item.version, null);
					Institution inst = new Institution();
					inst.setFilestoreId(shortName);
					itemFile.setInstitution(inst);
					String htmlFile = "_activity/" + FileSystemHelper.encode(activity.description + ".html");
					String destDir = "_mypages/" + activity.uuid;
					String destFile = destDir + "/page.html";
					if( !fileSystemService.fileExists(itemFile, destFile) )
					{
						fileSystemService.mkdir(itemFile, destDir);
						fileSystemService.rename(itemFile, htmlFile, destFile);
					}
					if( fileSystemService.fileExists(itemFile, destFile) )
					{
						activity.value1 = Long.toString(fileSystemService.fileLength(itemFile, destFile));
					}
					else
					{
						result.addLogEntry(new MigrationStatusLog(LogType.WARNING, KEY_PREFIX + "missingactivity",
							htmlFile, item.uuid, item.version));
						activity.value1 = "0";
					}
					activity.data = null;
				}
				else
				{
					ItemId itemIdKey;
					String extra;

					if( !Check.isEmpty(activity.url) )
					{
						String url = activity.url.substring(activity.url.indexOf("/items/") + 7);
						int verindex = url.indexOf('/');
						String uuid = url.substring(0, verindex);
						int endindex = url.indexOf('/', verindex + 1);
						int version = Integer.parseInt(url.substring(verindex + 1, endindex));
						itemIdKey = new ItemId(uuid, version);

						extra = URLDecoder.decode(url.substring(endindex + 1), "UTF-8"); //$NON-NLS-1$
						if( extra.isEmpty() )
						{
							extra = "viewdefault.jsp"; //$NON-NLS-1$
						}
					}
					else
					{
						itemIdKey = new ItemId(activity.value2);
						extra = activity.value3;
					}

					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("uuid", itemIdKey.getUuid());
					dataMap.put("version", itemIdKey.getVersion());
					dataMap.put("type", "p");
					activity.type = "custom";
					activity.url = extra;
					activity.value1 = "resource";
					activity.value2 = null;
					activity.value3 = null;
					activity.data = xmlService.serialiseToXml(dataMap);
				}
				FakeNavigationNode node = new FakeNavigationNode();
				node.index = index++;
				node.item = item;
				node.uuid = UUID.randomUUID().toString();
				node.name = activity.description;
				FakeNavigationTab tab = new FakeNavigationTab();
				tab.attachment = activity;
				tab.node = node;
				tab.name = "Created for Plan";
				tab.tabindex = 0;
				session.save(node);
				session.save(tab);
				session.save(activity);
				result.incrementStatus();

			}
			session.flush();
			session.clear();
		}
	}

	private void removeWizardAttributes(HasWizard hasWizard, Session session, MigrationResult result) throws Exception
	{
		final String wizardXml = hasWizard.getWizard();
		if( !Check.isEmpty(wizardXml) )
		{
			PropBagEx xml = new PropBagEx(wizardXml);

			removeAllNodesWithName(xml, "background");
			removeAllNodesWithName(xml, "styles");
			removeAllNodesWithName(xml, "help");

			hasWizard.setWizard(xml.toString());
			session.update(hasWizard);
			session.flush();
		}
		result.incrementStatus();
	}

	private void removeAllNodesWithName(PropBagEx xml, String name)
	{
		final Iterator<PropBagEx> x = xml.iterateAllNodesWithName(name);
		while( x.hasNext() )
		{
			x.next();
			x.remove();
		}
	}

	public interface HasWizard
	{
		String getWizard();

		void setWizard(String wizard);
	}

	@AccessType("field")
	@Entity(name = "ActivityWizard")
	public static class FakeActivityWizard extends FakeBaseEntity
	{
		@CollectionOfElements(fetch = FetchType.LAZY)
		@Fetch(value = FetchMode.SUBSELECT)
		@JoinColumn(name = "actwiz_id")
		List<WebLink> links = new ArrayList<WebLink>();

		@Embeddable
		@AccessType("field")
		public static class WebLink
		{
			@Property(key = "url")
			String url;

			@Property(key = "global")
			boolean global;

			@Property(key = "title")
			String title;

			@Property(key = "description")
			String description;
		}
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		long id;

		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		FakeLanguageBundle description;
		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		FakeLanguageBundle name;

		@JoinColumn
		@ElementCollection(fetch = FetchType.EAGER)
		@CollectionTable(name = "base_entity_attributes", joinColumns = @JoinColumn(name = "base_entity_id") )
		@Fetch(value = FetchMode.SUBSELECT)
		List<Attribute> attributes;

		@Embeddable
		@AccessType("field")
		public static class Attribute implements Serializable
		{
			private static final long serialVersionUID = 1L;

			@Column(length = 64, nullable = false)
			String key;
			@Column(name = "value", length = 1024)
			String value;
		}
	}

	@Entity(name = "LanguageBundle")
	@AccessType("field")
	public static class FakeLanguageBundle
	{
		@Id
		long id;

		@OneToMany(cascade = CascadeType.ALL, mappedBy = "bundle")
		@Fetch(value = FetchMode.SELECT)
		@MapKey(name = "locale")
		Map<String, FakeLanguageString> strings;
	}

	@Entity(name = "LanguageString")
	@AccessType("field")
	public static class FakeLanguageString
	{
		@Id
		long id;

		@Column(length = 20, nullable = false)
		// @Index(name = "localeIndex")
		String locale;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		// @Index(name = "bundleIndex")
		FakeLanguageBundle bundle;
	}

	@Entity(name = "ItemDefinition")
	@AccessType("field")
	public static class FakeItemDefinition extends FakeBaseEntity
	{
		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		@Index(name = "collectionBlobs")
		FakeItemdefBlobs slow;
		String embeddedXslt;
	}

	@Entity(name = "PowerSearch")
	@AccessType("field")
	public static class FakePowerSearch extends FakeBaseEntity implements HasWizard
	{
		@Lob
		private String wizard;

		@Override
		public String getWizard()
		{
			return wizard;
		}

		@Override
		public void setWizard(String wizard)
		{
			this.wizard = wizard;
		}
	}

	@Entity(name = "ItemdefBlobs")
	@AccessType("field")
	public static class FakeItemdefBlobs implements Serializable, HasWizard
	{
		private static final long serialVersionUID = 1L;

		@Id
		long id;
		@Lob
		String wizard;
		@Lob
		String confirmationTemplate;

		@Override
		public String getWizard()
		{
			return wizard;
		}

		@Override
		public void setWizard(String wizard)
		{
			this.wizard = wizard;
		}
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class FakeItem
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		String uuid;
		int version;
		@Column(length = 40)
		String folder;
		String planMetadata;
		@ManyToOne
		FakeInstitution institution;
	}

	@Entity(name = "Institution")
	@AccessType("field")
	public static class FakeInstitution
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		String shortName;
	}

	@Entity(name = "Attachment")
	@AccessType("field")
	public static class FakeAttachment
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		String type;
		String data;
		String description;
		String thumbnail;
		String url;
		String uuid;
		String value1;
		String value2;
		String value3;
		@ManyToOne
		FakeItem item;
		int attindex;
	}

	@Entity(name = "ItemNavigationNode")
	@AccessType("field")
	public static class FakeNavigationNode
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		@ManyToOne
		FakeItem item;
		String icon;
		String identifier;
		int index;
		String name;
		String uuid;
		@ManyToOne
		FakeNavigationNode parent;
	}

	@Entity(name = "ItemNavigationTab")
	@AccessType("field")
	public static class FakeNavigationTab
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		int tabindex;
		String name;
		String viewer;
		@ManyToOne
		FakeAttachment attachment;
		@ManyToOne
		FakeNavigationNode node;
	}
}
