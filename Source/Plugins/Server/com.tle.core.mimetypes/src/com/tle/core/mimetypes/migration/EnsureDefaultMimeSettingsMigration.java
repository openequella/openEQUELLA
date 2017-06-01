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

package com.tle.core.mimetypes.migration;

import java.util.ArrayList;
import java.util.Collection;
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

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.MapKeyType;
import org.hibernate.annotations.Type;
import org.hibernate.classic.Session;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.institution.MimeMigrator;
import com.tle.core.plugins.impl.PluginServiceImpl;

@SuppressWarnings("nls")
@Bind
@Singleton
public class EnsureDefaultMimeSettingsMigration extends AbstractHibernateDataMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(EnsureDefaultMimeSettingsMigration.class)
		+ ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migration.ensuredefaultmimesettings.title");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		List<MimeEntry> defaultTypes = MimeMigrator.getDefaultMimeEntries();
		List<FakeInstitution> institutions = session.createQuery("FROM Institution").list();

		for( MimeEntry def : defaultTypes )
		{
			String defaultIcon = def.getAttribute(MimeTypeConstants.KEY_ICON_PLUGINICON);

			List<FakeMimeEntry> mimes = session.createQuery("FROM MimeEntry WHERE type = :type")
				.setParameter("type", def.getType()).list();

			Map<FakeInstitution, FakeMimeEntry> instMimeMap = new HashMap<FakeInstitution, FakeMimeEntry>();
			for( FakeMimeEntry mime : mimes )
			{
				instMimeMap.put(mime.institution, mime);
			}

			// see if each inst has the required MIME types:
			for( FakeInstitution inst : institutions )
			{
				FakeMimeEntry m = instMimeMap.get(inst);
				if( m == null )
				{
					m = new FakeMimeEntry();
					m.setInstitution(inst);
					m.setDescription(def.getDescription());
					m.setExtensions(new ArrayList<String>(def.getExtensions()));
					m.setType(def.getType());
					m.setAttributes(new HashMap<String, String>(def.getAttributes()));
					session.save(m);
				}
				else
				{
					// ensure the default icon at least
					String icon = m.getAttribute(MimeTypeConstants.KEY_ICON_PLUGINICON);
					if( Check.isEmpty(icon) && !Check.isEmpty(defaultIcon) )
					{
						m.setAttribute(MimeTypeConstants.KEY_ICON_PLUGINICON, defaultIcon);
						session.save(m);
					}
				}
			}
		}

		result.incrementStatus();
		session.flush();
		session.clear();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1; // it's too expensive to work out
	}

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

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "mimeInstitutionIndex")
		FakeInstitution institution;

		@ElementCollection
		@Column(name = "element", length = 20)
		@CollectionTable(name = "mime_entry_extensions", joinColumns = @JoinColumn(name = "mime_entry_id"))
		Collection<String> extensions = new ArrayList<String>();

		@Column(length = 100, nullable = false)
		String type;

		@Column(length = 512)
		String description;

		@ElementCollection
		@Column(name = "element", nullable = false)
		@CollectionTable(name = "mime_entry_attributes", joinColumns = @JoinColumn(name = "mime_entry_id"))
		@Lob
		@MapKeyColumn(name = "mapkey", length = 100, nullable = false)
		@MapKeyType(@Type(type = "string"))
		Map<String, String> attributes = new HashMap<String, String>();

		public void setId(long id)
		{
			this.id = id;
		}

		public void setInstitution(FakeInstitution institution)
		{
			this.institution = institution;
		}

		public void setExtensions(Collection<String> extensions)
		{
			this.extensions = extensions;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public void setAttributes(Map<String, String> attributes)
		{
			this.attributes = attributes;
		}

		public void setAttribute(String key, String value)
		{
			this.attributes.put(key, value);
		}

		public String getAttribute(String key)
		{
			return attributes.get(key);
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
