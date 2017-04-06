package com.tle.core.kaltura.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;
import org.hibernate.classic.Session;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.Check;
import com.tle.common.kaltura.KalturaUtils;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

@Bind
@Singleton
@SuppressWarnings({"nls", "unchecked", "deprecation"})
public class UpdateKalturaAttachmentsMigration extends AbstractHibernateDataMigration
{

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.kaltura.migration.info.attachments");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		ScrollableResults results = session.createQuery(
			"FROM Item i LEFT JOIN i.attachments a WHERE a.type = 'custom' AND a.value1 = 'kaltura'").scroll();

		while( results.next() )
		{
			// Item
			FakeItem item = (FakeItem) results.get()[0];
			List<FakeAttachment> attachments = item.attachments;

			// Get kaltura server
			KalturaServer ks = getKalturaServer(session, item.institution);

			if( ks != null )
			{
				for( FakeAttachment att : attachments )
				{
					// Set details on attachment
					att.setData(KalturaUtils.PROPERTY_KALTURA_SERVER, ks.getUuid());

					// Remove old data
					att.data.remove(KalturaUtils.PROPERTY_DATA_URL);
				}
				session.flush();
				session.clear();
			}

			result.incrementStatus();
		}
	}

	private KalturaServer getKalturaServer(Session session, Institution inst)
	{
		Query kquery = session.createQuery("FROM KalturaServer WHERE institution = :inst AND end_point = :endpoint");
		kquery.setParameter("endpoint", KalturaUtils.KALTURA_SAAS_ENDPOINT);
		kquery.setParameter("inst", inst);
		List<KalturaServer> ksList = kquery.list();

		return !Check.isEmpty(ksList) ? ksList.get(0) : null;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM Item i LEFT JOIN i.attachments a WHERE a.type = 'custom' AND a.value1 = 'kaltura'");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{KalturaServer.class, BaseEntity.class, BaseEntity.Attribute.class, LanguageBundle.class,
				Institution.class, LanguageString.class, FakeItem.class, FakeAttachment.class};
	}

	@Entity(name = "Attachment")
	@AccessType("field")
	public static class FakeAttachment
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Column(length = 40, nullable = false)
		@Index(name = "attachmentUuidIndex")
		String uuid;

		@ManyToOne
		@JoinColumn(name = "item_id", insertable = false, updatable = false, nullable = false)
		@XStreamOmitField
		@Index(name = "attachmentItem")
		FakeItem item;

		@Column(length = 31)
		String type;

		@Column(length = 1024)
		String value1;

		@Column(length = 1024)
		String value2;

		@Column(length = 1024)
		String url;

		@Column(length = 1024)
		String description;

		@Type(type = "xstream_immutable")
		@Column(length = 8192)
		Map<String, Object> data;
		private transient boolean dataModified;

		public void setData(String name, Object value)
		{
			if( data == null )
			{
				data = new HashMap<String, Object>();
			}
			else if( !dataModified )
			{
				data = new HashMap<String, Object>(data);
			}
			dataModified = true;
			data.put(name, value);
		}

		public Object getData(String name)
		{
			return data == null ? null : data.get(name);
		}
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class FakeItem
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Index(name = "itemUuidIndex")
		@Column(length = 40)
		String uuid;

		@Index(name = "itemVersionIndex")
		int version;

		@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
		@IndexColumn(name = "attindex")
		@Fetch(value = FetchMode.SUBSELECT)
		@JoinColumn(name = "item_id", nullable = false)
		List<FakeAttachment> attachments = new ArrayList<FakeAttachment>();

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.EAGER)
		@Index(name = "itemInstitutionIndex")
		@XStreamOmitField
		Institution institution;

		public Institution getInstitution()
		{
			return institution;
		}

		public void setInstitution(Institution institution)
		{
			this.institution = institution;
		}

	}

}
