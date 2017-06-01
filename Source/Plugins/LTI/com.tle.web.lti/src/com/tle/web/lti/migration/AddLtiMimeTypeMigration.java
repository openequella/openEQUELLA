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

package com.tle.web.lti.migration;

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

import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.web.viewurl.ResourceViewerConfig;

/**
 * @author larry
 *
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class AddLtiMimeTypeMigration extends AbstractHibernateDataMigration
{
	private static final String COUNT = "SELECT COUNT(*) FROM MimeEntry WHERE type = '" + ExternalToolConstants.MIME_TYPE
		+ "' and institution = :institution";
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AddLtiMimeTypeMigration.class) + ".addlti.";

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	/**
	 * @see com.tle.core.migration.Migration#createMigrationInfo()
	 */
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}

	/**
	 * @see com.tle.core.migration.AbstractHibernateDataMigration#executeDataMigration(com.tle.core.hibernate.impl.HibernateMigrationHelper,
	 *      com.tle.core.migration.MigrationResult,
	 *      org.hibernate.classic.Session)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		List<FakeInstitution> institutions = session.createQuery("FROM Institution").list();
		for( FakeInstitution insti : institutions )
		{
			int count = count(session.createQuery(COUNT).setParameter("institution", insti));
			if( count == 0 )
			{
				FakeMimeEntry me = new FakeMimeEntry();
				me.setInstitution(insti);
				me.setDescription(ExternalToolConstants.MIME_DESC);
				me.setType(ExternalToolConstants.MIME_TYPE);
				me.setAttribute(MimeTypeConstants.KEY_ICON_PLUGINICON, ExternalToolConstants.MIME_ICON_PATH);

				me.setAttribute(MimeTypeConstants.KEY_DEFAULT_VIEWERID, ExternalToolConstants.VIEWER_ID);
				// Only one viewer ...?
				me.setAttribute(MimeTypeConstants.KEY_ENABLED_VIEWERS, "[\"" + ExternalToolConstants.VIEWER_ID + "\"]");
				me.setAttribute(MimeTypeConstants.KEY_DISABLE_FILEVIEWER, "true");

				ResourceViewerConfig rvc = new ResourceViewerConfig();
				rvc.setThickbox(false);
				rvc.setWidth("800");
				rvc.setHeight("600");
				rvc.setOpenInNewWindow(true);

				setBeanAttribute(me, "viewerConfig-" + ExternalToolConstants.VIEWER_ID, rvc);

				session.save(me);
			}
			result.incrementStatus();
		}
		session.flush();
		session.clear();
	}

	/**
	 * @see com.tle.core.migration.AbstractHibernateDataMigration#countDataMigrations(com.tle.core.hibernate.impl.HibernateMigrationHelper,
	 *      org.hibernate.classic.Session)
	 */
	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM Institution");
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

	/**
	 * @see com.tle.core.migration.AbstractHibernateDataMigration#getDomainClasses()
	 */
	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeMimeEntry.class, FakeInstitution.class};
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

		public Map<String, String> getAttributes()
		{
			return attributes;
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
	}

	@Entity(name = "Institution")
	@AccessType("field")
	public static class FakeInstitution
	{
		@Id
		long id;
	}
}
