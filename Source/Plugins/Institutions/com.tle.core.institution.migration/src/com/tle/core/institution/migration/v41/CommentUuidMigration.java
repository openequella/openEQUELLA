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

package com.tle.core.institution.migration.v41;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CommentUuidMigration extends AbstractHibernateSchemaMigration
{
	private static final int BATCH_SIZE = 1000;
	private static final String TABLE = "comments";
	private static final String COLUMN = "uuid";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.migration.commentuuid.title");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeComment.class, FakeItem.class,};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = new ArrayList<String>();
		sql.addAll(helper.getAddColumnsSQL(TABLE, COLUMN));
		return sql;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> dropModify = new ArrayList<String>();
		dropModify.addAll(helper.getAddNotNullSQL(TABLE, COLUMN));
		dropModify.addAll(helper.getAddIndexesAndConstraintsForColumns(TABLE, COLUMN));
		return dropModify;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM Comment");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		List<FakeComment> comments = session.createQuery("FROM Comment").list();
		session.clear();

		int i = 0;
		for( FakeComment c : comments )
		{
			c.uuid = UUID.randomUUID().toString();
			if( Check.isEmpty(c.comment) )
			{
				c.comment = null;
			}
			session.update(c);
			i++;
			if( i % BATCH_SIZE == 0 )
			{
				session.flush();
				session.clear();
			}
			result.incrementStatus();
		}
		session.flush();
		session.clear();
	}

	@Entity(name = "Comment")
	@Table(name = "comments", uniqueConstraints = {@UniqueConstraint(columnNames = {"item_id", "uuid"})})
	@AccessType("field")
	public static class FakeComment
	{
		@Id
		long id;
		@Column(length = 40, nullable = false)
		@Index(name = "commentUuidIndex")
		String uuid;
		@Lob
		String comment;
		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		FakeItem item;
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class FakeItem
	{
		@Id
		long id;
	}
}
