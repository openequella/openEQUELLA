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

package com.tle.web.viewitem.htmlfiveviewer.migration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import net.sf.json.JSONArray;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.MapKeyType;
import org.hibernate.annotations.Type;
import org.hibernate.classic.Session;

import com.google.inject.Singleton;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.mimetypes.MimeTypeConstants;

@Bind
@Singleton
@SuppressWarnings("nls")
public class HtmlFiveMimeTypeAndPlayerMigration extends AbstractHibernateDataMigration
{

	private static final String HTML_FIVE_VIEWER_ID = "htmlFiveViewer";
	private static final String FROM = "From MimeEntry WHERE type = 'video/mp4' OR type = 'video/ogg' OR type = 'video/webm' AND institution = :institution";
	private static final String COUNT_WEBM = "SELECT COUNT(*) FROM MimeEntry WHERE type = 'video/webm' and institution = :institution";
	private static final String COUNT_OGG = "SELECT COUNT(*) FROM MimeEntry WHERE type = 'video/ogg' and institution = :institution";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.web.viewitem.htmlfiveviewer.defaultmigration.title");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Add ogg/webm if don't exist
		List<FakeInstitution> institutions = session.createQuery("FROM Institution").list();
		for( FakeInstitution inst : institutions )
		{
			if( count(session.createQuery(COUNT_OGG).setParameter("institution", inst)) == 0 )
			{
				FakeMimeEntry ogg = new FakeMimeEntry();
				ogg.setInstitution(inst);
				ogg.setType("video/ogg");
				ogg.addExtensions("ogv", "ogg");
				ogg.setDescription("Video");

				session.save(ogg);
			}
			if (count(session.createQuery(COUNT_WEBM).setParameter("institution", inst)) == 0)
			{
				FakeMimeEntry webm = new FakeMimeEntry();
				webm.setInstitution(inst);
				webm.setType("video/webm");
				webm.addExtensions("webm");
				webm.setDescription("Video");
				
				session.save(webm);
			}

			// Default Viewer option
			final List<FakeMimeEntry> entries = session.createQuery(FROM).setParameter("institution", inst).list();
			for( FakeMimeEntry entry : entries )
			{
				final Map<String, String> attributes = entry.attributes;
				attributes.put(MimeTypeConstants.KEY_DEFAULT_VIEWERID, HTML_FIVE_VIEWER_ID);
				final String enabledViewersJson = attributes.get(MimeTypeConstants.KEY_ENABLED_VIEWERS);
				final List<String> viewers;
				if( Check.isEmpty(enabledViewersJson) )
				{
					viewers = new ArrayList<String>();
				}
				else
				{
					viewers = new ArrayList<String>(JSONArray.toCollection(JSONArray.fromObject(enabledViewersJson),
						String.class));
				}
				viewers.add(HTML_FIVE_VIEWER_ID);
				attributes.put(MimeTypeConstants.KEY_ENABLED_VIEWERS, JSONArray.fromObject(viewers).toString());
				attributes.put(MimeTypeConstants.KEY_ICON_PLUGINICON, "icons/video.png");
				session.update(entry);

			}
			result.incrementStatus();
		}
		session.flush();
		session.clear();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "From Institution");
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

		@ElementCollection
		@Column(name = "element", length = 20)
		@CollectionTable(name = "mime_entry_extensions", joinColumns = @JoinColumn(name = "mime_entry_id"))
		Collection<String> extensions = new ArrayList<String>();

		@Column(length = 100, nullable = false)
		String type;

		@Column(length = 512)
		String description;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "mimeInstitutionIndex")
		FakeInstitution institution;

		public void setType(String type)
		{
			this.type = type;
		}

		public void setInstitution(FakeInstitution institution)
		{
			this.institution = institution;
		}

		public void addExtensions(String... exts)
		{
			for( String ext : exts )
			{
				extensions.add(ext);
			}
		}

		public void setDescription(String description)
		{
			this.description = description;
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
