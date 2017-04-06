package com.tle.web.kaltura.migration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;

import net.sf.json.JSONObject;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.MapKeyType;
import org.hibernate.annotations.Type;
import org.hibernate.classic.Session;

import com.google.common.collect.Maps;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.web.viewurl.ResourceViewerConfig;

@SuppressWarnings("nls")
@Bind
@Singleton
public class AddKalturaMimeTypeMigration extends AbstractHibernateDataMigration
{
	private static final String COUNT = "SELECT COUNT(*) FROM MimeEntry WHERE type = 'equella/attachment-kaltura' and institution = :institution";

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.web.kaltura.migration.mime.title");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		List<FakeInstitution> institutions = session.createQuery("FROM Institution").list();
		for( FakeInstitution inst : institutions )
		{
			int count = count(session.createQuery(COUNT).setParameter("institution", inst));
			if( count == 0 )
			{
				FakeMimeEntry me = new FakeMimeEntry();
				me.setInstitution(inst);
				me.setDescription("Kaltura media");
				me.setType("equella/attachment-kaltura");
				me.setAttribute(MimeTypeConstants.KEY_DEFAULT_VIEWERID, "kalturaViewer");
				me.setAttribute(MimeTypeConstants.KEY_ENABLED_VIEWERS, "[\"kalturaViewer\"]");
				me.setAttribute(MimeTypeConstants.KEY_DISABLE_FILEVIEWER, "true");

				ResourceViewerConfig rvc = new ResourceViewerConfig();
				rvc.setThickbox(true);
				rvc.setWidth("800");
				rvc.setHeight("600");
				rvc.setOpenInNewWindow(true);

				HashMap<String, Object> attrs = Maps.newHashMap();
				attrs.put("kalturaWidth", "800");
				attrs.put("kalturaHeight", "600");
				rvc.setAttr(attrs);

				setBeanAttribute(me, "viewerConfig-kalturaViewer", rvc);
				session.save(me);
			}
			result.incrementStatus();
		}
		session.flush();
		session.clear();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM Institution");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeMimeEntry.class, FakeInstitution.class};
	}

	private <T> void setBeanAttribute(FakeMimeEntry entry, String key, T bean)
	{
		Map<String, String> attr = entry.getAttributes();
		if( bean == null )
		{
			attr.remove(key);
			return;
		}
		attr.put(key, JSONObject.fromObject(bean).toString());
	}

	@Entity(name = "MimeEntry")
	@AccessType("field")
	public static class FakeMimeEntry
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@ElementCollection
		@Column(name = "element", nullable = false)
		@CollectionTable(name = "mime_entry_attributes", joinColumns = @JoinColumn(name = "mime_entry_id"))
		@Lob
		@MapKeyColumn(name = "mapkey", length = 100, nullable = false)
		@MapKeyType(@Type(type = "string"))
		Map<String, String> attributes = new HashMap<String, String>();

		@Column(length = 100, nullable = false)
		String type;

		@Column(length = 512)
		String description;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "mimeInstitutionIndex")
		FakeInstitution institution;

		public void setAttribute(String key, String value)
		{
			this.attributes.put(key, value);
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public void setInstitution(FakeInstitution institution)
		{
			this.institution = institution;
		}

		public Map<String, String> getAttributes()
		{
			return attributes;
		}
	}

	@Entity(name = "Institution")
	@AccessType("field")
	public static class FakeInstitution
	{
		@Id
		long id;
	}
}
