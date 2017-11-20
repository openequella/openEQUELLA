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

package com.tle.web.scorm.migration;

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

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

@SuppressWarnings("nls")
@Bind
@Singleton
public class AddScormMimeTypeDatabaseMigration extends AbstractHibernateDataMigration
{
	private static final String COUNT = "SELECT COUNT(*) FROM MimeEntry WHERE type = 'equella/scorm-package' and institution = :institution";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.web.scorm.migration.mime.title");
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
				FakeMimeEntry mimeEntry = new FakeMimeEntry();
				mimeEntry.setInstitution(inst);
				mimeEntry.setDescription("SCORM Package");
				mimeEntry.setType("equella/scorm-package");
				mimeEntry.setAttribute("enabledViewers", "[\"downloadIms\"]");
				mimeEntry.setAttribute("PluginIconPath", "icons/ims.png");

				session.save(mimeEntry);

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
		private String type;

		@Column(length = 512)
		private String description;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "mimeInstitutionIndex")
		private FakeInstitution institution;

		public void setAttribute(String key, String value)
		{
			this.attributes.put(key, value);
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public String getDescription()
		{
			return description;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public String getType()
		{
			return type;
		}

		public void setInstitution(FakeInstitution institution)
		{
			this.institution = institution;
		}

		public FakeInstitution getInstitution()
		{
			return institution;
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
