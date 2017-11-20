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

import java.io.Reader;
import java.io.StringWriter;
import java.util.List;

import javax.inject.Inject;
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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.dytech.common.io.UnicodeReader;
import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.dytech.edge.common.Constants;
import com.google.common.io.CharStreams;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileHandleUtils;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.services.FileSystemService;

/**
 * @author Andrew Gibb
 */

@Bind
@Singleton
@SuppressWarnings("nls")
public class ConvertXsltTemplateFileToString extends AbstractHibernateDataMigration
{
	@Inject
	private FileSystemService fileSystemService;

	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(ConvertXsltTemplateFileToString.class)
		+ ".convertxsltfiletostring.";

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		Query countQry = session
			.createQuery("SELECT COUNT(*) FROM ItemDefinition WHERE slow.itemSummarySections LIKE :query");
		countQry.setParameter("query", "%xsltSection%");
		return count(countQry);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		Query dataQry = session.createQuery("FROM ItemDefinition WHERE slow.itemSummarySections LIKE :query");
		dataQry.setParameter("query", "%xsltSection%");

		final List<FakeItemDefinition> ids = dataQry.list();

		for( FakeItemDefinition id : ids )
		{
			// Do conversion
			convertXslt(id, session, result);
		}
	}

	private void convertXslt(FakeItemDefinition id, Session session, MigrationResult result) throws Exception
	{
		final String sectionsXml = id.slow.getItemSummarySections();

		if( !Check.isEmpty(sectionsXml) )
		{
			// Summary sections XML
			PropBagEx xml = new PropBagEx(sectionsXml);

			// Check through all available Summary Sections
			PropBagIterator iter = xml.iterator("configList/com.tle.beans.entity.itemdef.SummarySectionsConfig");

			for( PropBagEx config : iter )
			{
				boolean changed = false;

				if( config.getNode("value").equals("xsltSection") )
				{
					// Get XSLT template path
					String filePath = config.getNode("configuration");
					FakeEntityFile item = new FakeEntityFile(id.id, id.institution.filestoreId);

					// Read contents to string and close stream
					if( fileSystemService.fileExists(item, filePath) )
					{
						try( Reader reader = new UnicodeReader(fileSystemService.read(item, filePath), "UTF-8") )
						{
							StringWriter writer = new StringWriter();
							CharStreams.copy(reader, writer);
							String xsltContent = writer.getBuffer().toString();

							// Update XML
							config.setNode("configuration", xsltContent);
						}
						// Delete XSLT file
						fileSystemService.removeFile(item, filePath);
					}
					else
					{
						config.setNode("configuration", Constants.BLANK);
					}

					changed = true;
				}

				// Update Database
				if( changed )
				{
					id.slow.setItemSummarySections(xml.toString());
					session.update(id);
					session.flush();
				}
			}
		}
		result.incrementStatus();
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeItemdefBlobs.class, FakeItemDefinition.class, FakeBaseEntity.class,
				FakeInstitution.class};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}

	public class FakeEntityFile implements FileHandle
	{
		long id;
		String filestoreId;

		public FakeEntityFile(long id, String filestoreId)
		{
			this.id = id;
			this.filestoreId = filestoreId;
		}

		@Override
		public String getAbsolutePath()
		{
			return PathUtils.filePath("Institutions", this.filestoreId, "Templates", FileHandleUtils.getHashedPath(id));
		}

		@Override
		public String getMyPathComponent()
		{
			return "";
		}

		@Override
		public String getFilestoreId()
		{
			return null;
		}
	}

	@Entity(name = "ItemdefBlobs")
	@AccessType("field")
	public static class FakeItemdefBlobs
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Lob
		public String itemSummarySections;

		public String getItemSummarySections()
		{
			return itemSummarySections;
		}

		public void setItemSummarySections(String itemSummarySections)
		{
			this.itemSummarySections = itemSummarySections;
		}
	}

	@Entity(name = "ItemDefinition")
	@AccessType("field")
	public static class FakeItemDefinition extends FakeBaseEntity
	{
		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		@Index(name = "collectionBlobs")
		FakeItemdefBlobs slow;
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "institutionIndex")
		FakeInstitution institution;
	}

	@Entity(name = "Institution")
	@AccessType("field")
	public static class FakeInstitution
	{
		@Id
		long id;

		@Column(name = "shortName", unique = true, length = 20, nullable = false)
		String filestoreId;
	}
}
