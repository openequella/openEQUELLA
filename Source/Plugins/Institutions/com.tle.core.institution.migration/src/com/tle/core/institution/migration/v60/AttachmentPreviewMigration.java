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

package com.tle.core.institution.migration.v60;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.ScrollableResults;
import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.xml.service.XmlService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AttachmentPreviewMigration extends AbstractHibernateSchemaMigration
{
	private static final String TABLE_ATTACHMENT = "attachment";
	private static final String COL_PREVIEW = "preview";
	private static final String KEY_PREVIEW = "preview";

	@Inject
	private XmlService xmlService;

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.migration.v60.prvattach.title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		session.createQuery("update Attachment set preview = false").executeUpdate();
		result.incrementStatus();
		ScrollableResults scroll = session
			.createQuery("from Attachment where data like '%<string>" + KEY_PREVIEW + "</string>%'").scroll();
		while( scroll.next() )
		{
			FakeAttachment attachment = (FakeAttachment) scroll.get(0);
			Map<Object, Object> dataMap = xmlService.deserialiseFromXml(getClass().getClassLoader(), attachment.data);
			attachment.preview = Boolean.TRUE.equals(dataMap.get(KEY_PREVIEW));
			dataMap.remove(KEY_PREVIEW);
			if( dataMap.isEmpty() )
			{
				attachment.data = null;
			}
			else
			{
				attachment.data = xmlService.serialiseToXml(dataMap);
			}
			session.save(attachment);
			session.flush();
			session.clear();
			result.incrementStatus();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1 + count(session, "from Attachment where data like '%<string>" + KEY_PREVIEW + "</string>%'");
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getAddNotNullSQL(TABLE_ATTACHMENT, COL_PREVIEW);
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL(TABLE_ATTACHMENT, COL_PREVIEW);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeAttachment.class};
	}

	@Entity(name = "Attachment")
	@AccessType("field")
	public static class FakeAttachment
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		@Column(length = 8192)
		String data;
		Boolean preview;
	}

}
