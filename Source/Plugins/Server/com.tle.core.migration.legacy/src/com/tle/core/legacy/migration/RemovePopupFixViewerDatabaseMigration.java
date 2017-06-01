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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import net.sf.json.JSONArray;

import org.hibernate.classic.Session;

import com.tle.beans.Institution;
import com.tle.beans.mime.MimeEntry;
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
public class RemovePopupFixViewerDatabaseMigration extends AbstractHibernateDataMigration
{
	private static final String VIEWER_ID = "movPopupFixViewer";
	private static final String FROM = "FROM MimeEntry WHERE CAST(attributes['"
		+ MimeTypeConstants.KEY_DEFAULT_VIEWERID + "'] AS string) = '" + VIEWER_ID + "'" + " OR CAST(attributes['"
		+ MimeTypeConstants.KEY_ENABLED_VIEWERS + "'] AS string) LIKE '%" + VIEWER_ID + "%'";

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, FROM);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		final List<MimeEntry> mimes = session.createQuery(FROM).list();
		for( MimeEntry mime : mimes )
		{
			final Map<String, String> attributes = mime.getAttributes();

			final String defaultViewer = attributes.get(MimeTypeConstants.KEY_DEFAULT_VIEWERID);
			if( !Check.isEmpty(defaultViewer) && defaultViewer.equals(VIEWER_ID) )
			{
				attributes.put(MimeTypeConstants.KEY_DEFAULT_VIEWERID, MimeTypeConstants.VAL_DEFAULT_VIEWERID);
			}

			final String enabledViewersJson = attributes.get(MimeTypeConstants.KEY_ENABLED_VIEWERS);
			if( !Check.isEmpty(enabledViewersJson) )
			{
				final List<String> viewers = new ArrayList<String>(JSONArray.toCollection(
					JSONArray.fromObject(enabledViewersJson), String.class));
				viewers.remove(VIEWER_ID);
				attributes.put(MimeTypeConstants.KEY_ENABLED_VIEWERS, JSONArray.fromObject(viewers).toString());
			}

			session.save(mime);
			session.flush();
			session.clear();

			result.incrementStatus();
		}
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{Institution.class, MimeEntry.class};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.legacy.migration.movpopupfixviewer.migration.title");
	}
}
