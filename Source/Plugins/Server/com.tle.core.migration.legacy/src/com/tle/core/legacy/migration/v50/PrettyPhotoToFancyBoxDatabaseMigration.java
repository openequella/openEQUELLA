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

package com.tle.core.legacy.migration.v50;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import net.sf.json.JSONArray;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.MapKey;
import org.hibernate.classic.Session;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.mimetypes.MimeTypeConstants;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class PrettyPhotoToFancyBoxDatabaseMigration extends AbstractHibernateDataMigration
{
	private static final String OLD_VIEWER_ID = "pretty";
	private static final String NEW_VIEWER_ID = "fancy";
	private static final String FROM = "FROM MimeEntry WHERE CAST(attributes['"
		+ MimeTypeConstants.KEY_DEFAULT_VIEWERID + "'] AS string) = '" + OLD_VIEWER_ID + "'" + " OR CAST(attributes['"
		+ MimeTypeConstants.KEY_ENABLED_VIEWERS + "'] AS string) LIKE '%" + OLD_VIEWER_ID + "%'";

	private static final String FROM_ATTACHMENTS = "FROM Attachment WHERE viewer = '" + OLD_VIEWER_ID + "'";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.legacy.migration.prettytofancy.migration.title", "");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		final List<FakeMimeEntry> entries = session.createQuery(FROM).list();
		for( FakeMimeEntry entry : entries )
		{
			final Map<String, String> attributes = entry.attributes;
			final String defaultViewer = attributes.get(MimeTypeConstants.KEY_DEFAULT_VIEWERID);
			if( defaultViewer.equals(OLD_VIEWER_ID) )
			{
				attributes.put(MimeTypeConstants.KEY_DEFAULT_VIEWERID, NEW_VIEWER_ID);
			}

			final String enabledViewersJson = attributes.get(MimeTypeConstants.KEY_ENABLED_VIEWERS);
			if( !Check.isEmpty(enabledViewersJson) )
			{
				final List<String> viewers = new ArrayList<String>(JSONArray.toCollection(
					JSONArray.fromObject(enabledViewersJson), String.class));
				final int oldViewerIndex = viewers.indexOf(OLD_VIEWER_ID);
				if( oldViewerIndex >= 0 )
				{
					viewers.set(oldViewerIndex, NEW_VIEWER_ID);
					attributes.put(MimeTypeConstants.KEY_ENABLED_VIEWERS, JSONArray.fromObject(viewers).toString());
				}
			}

			session.update(entry);
			session.flush();

			result.incrementStatus();
		}

		final List<FakeAttachment> attachments = session.createQuery(FROM_ATTACHMENTS).list();
		for( FakeAttachment attachment : attachments )
		{
			attachment.viewer = NEW_VIEWER_ID;
			session.update(attachment);
			session.flush();

			result.incrementStatus();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, FROM) + count(session, FROM_ATTACHMENTS);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeMimeEntry.class, FakeAttachment.class};
	}

	@Entity(name = "MimeEntry")
	@AccessType("field")
	public static class FakeMimeEntry
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@CollectionOfElements
		@Column(nullable = false)
		@MapKey(columns = {@Column(length = 100, nullable = false)})
		Map<String, String> attributes = new HashMap<String, String>();
	}

	@Entity(name = "Attachment")
	@AccessType("field")
	public static class FakeAttachment
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		String viewer;
	}
}
