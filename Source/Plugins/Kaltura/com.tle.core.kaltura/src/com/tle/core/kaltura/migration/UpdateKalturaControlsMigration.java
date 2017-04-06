package com.tle.core.kaltura.migration;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.beans.Institution;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.Check;
import com.tle.common.kaltura.KalturaUtils;
import com.tle.common.util.XmlDocument;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

@Bind
@Singleton
@SuppressWarnings({"nls", "unchecked"})
public class UpdateKalturaControlsMigration extends AbstractHibernateDataMigration
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.kaltura.migration.info.controls");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		// Get all collections
		final List<FakeItemDefinition> itemDefs = session.createQuery("FROM ItemDefinition").list();

		for( FakeItemDefinition itemDef : itemDefs )
		{
			// Get default Kaltura.com SaaS server
			Query kquery = session
				.createQuery("FROM KalturaServer WHERE institution = :inst AND end_point = :endpoint");
			kquery.setParameter("endpoint", KalturaUtils.KALTURA_SAAS_ENDPOINT);
			kquery.setParameter("inst", itemDef.getInstitution());
			List<FakeKalturaServer> ksList = kquery.list();

			final FakeItemdefBlobs blob = itemDef.getSlow();
			XmlDocument xmlDoc = new XmlDocument(blob.getWizard());

			// Is there a Kaltura Server?
			if( !Check.isEmpty(ksList) )
			{
				if( UpdateKalturaControlsXmlMigration.addKalturaServerAttributes(xmlDoc,
					xmlDoc.node("com.tle.beans.entity.itemdef.Wizard"), ksList.get(0).getUuid()) )
				{
					blob.setWizard(xmlDoc.toString());
					session.save(blob);
				}
			}
			result.incrementStatus();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM ItemDefinition");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeKalturaServer.class, Institution.class, FakeItemDefinition.class, FakeBaseEntity.class,
				FakeItemdefBlobs.class, LanguageBundle.class, Institution.class, LanguageString.class};
	}

	@Entity(name = "KalturaServer")
	@AccessType("field")
	public static class FakeKalturaServer extends FakeBaseEntity
	{

	}

	@Entity(name = "ItemDefinition")
	@AccessType("field")
	public static class FakeItemDefinition extends FakeBaseEntity
	{
		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		@Index(name = "collectionBlobs")
		private FakeItemdefBlobs slow;

		public FakeItemdefBlobs getSlow()
		{
			return slow;
		}

		public void setSlow(FakeItemdefBlobs slow)
		{
			this.slow = slow;
		}
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private long id;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "institutionIndex")
		private Institution institution;

		@Column(length = 40, nullable = false)
		@Index(name = "uuidIndex")
		private String uuid;

		public Institution getInstitution()
		{
			return institution;
		}

		public void setInstitution(Institution institution)
		{
			this.institution = institution;
		}

		public long getId()
		{
			return id;
		}

		public void setId(long id)
		{
			this.id = id;
		}

		public String getUuid()
		{
			return uuid;
		}

		public void setUuid(String uuid)
		{
			this.uuid = uuid;
		}
	}

	@Entity(name = "ItemdefBlobs")
	@AccessType("field")
	public static class FakeItemdefBlobs
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private long id;

		private String wizard;

		public long getId()
		{
			return id;
		}

		public void setId(long id)
		{
			this.id = id;
		}

		public String getWizard()
		{
			return wizard;
		}

		public void setWizard(String wizard)
		{
			this.wizard = wizard;
		}
	}
}
