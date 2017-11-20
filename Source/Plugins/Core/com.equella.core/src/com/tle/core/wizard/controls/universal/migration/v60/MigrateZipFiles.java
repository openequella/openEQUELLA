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

package com.tle.core.wizard.controls.universal.migration.v60;

import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.annotations.Type;
import org.hibernate.classic.Session;

import com.google.common.collect.Maps;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.common.i18n.KeyString;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class MigrateZipFiles extends AbstractHibernateDataMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(MigrateZipFiles.class) + '.';

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(new KeyString(KEY_PREFIX + "migration.fixzips.title"));
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		Query query = session
			.createQuery("select distinct i.id from Item i left join i.attachments a where a.type = 'zip'");
		ScrollableResults scroll = query.scroll();
		while( scroll.next() )
		{
			Long itemId = scroll.getLong(0);
			List<FakeAttachment> attachments = session
				.createQuery("from Attachment where item.id = ? and type in ('zip', 'file')").setParameter(0, itemId)
				.list();
			Map<String, String> zipMap = Maps.newHashMap();
			for( FakeAttachment fakeAttachment : attachments )
			{
				if( "zip".equals(fakeAttachment.type) && fakeAttachment.url.startsWith("_zips/") )
				{
					String zipFile = fakeAttachment.url.substring(6);
					zipMap.put(zipFile, fakeAttachment.uuid);
				}
			}
			for( FakeAttachment fakeAttachment : attachments )
			{
				int afterSlash = fakeAttachment.url.indexOf('/');
				if( "file".equals(fakeAttachment.type) && afterSlash != -1 )
				{
					String zipFile = fakeAttachment.url.substring(0, afterSlash);
					String zipUuid = zipMap.get(zipFile);
					if( zipUuid != null )
					{
						Map<String, Object> data = Maps.newHashMap();
						if( fakeAttachment.data != null )
						{
							data.putAll(fakeAttachment.data);
						}
						data.put(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID, zipUuid);
						fakeAttachment.data = data;
						session.save(fakeAttachment);
					}
				}
			}
			session.flush();
			session.clear();
			result.incrementStatus();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session
			.createQuery("select count (distinct i.id) from Item as i left join i.attachments as a where a.type = 'zip'"));
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeItem.class, FakeAttachment.class};
	}

	@Entity(name = "Item")
	public static class FakeItem
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@OneToMany(fetch = FetchType.LAZY)
		@JoinColumn(name = "item_id", nullable = false)
		List<FakeAttachment> attachments;
	}

	@Entity(name = "Attachment")
	public static class FakeAttachment
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		String type;
		String url;
		String uuid;

		@Type(type = "xstream_immutable")
		@Column(length = 8192)
		Map<String, Object> data;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "item_id", insertable = false, updatable = false, nullable = false)
		FakeItem item;
	}
}
