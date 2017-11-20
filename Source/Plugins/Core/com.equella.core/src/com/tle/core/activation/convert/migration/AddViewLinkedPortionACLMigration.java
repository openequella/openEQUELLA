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

package com.tle.core.activation.convert.migration;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.common.security.SecurityConstants;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@SuppressWarnings("nls")
public class AddViewLinkedPortionACLMigration extends AbstractHibernateDataMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(AddViewLinkedPortionACLMigration.class)
		+ ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migration.acl.viewlinkedportion");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		@SuppressWarnings("unchecked")
		List<FakeAccessExpression> qr = session.createQuery("from AccessExpression where expression = '*'").list();
		FakeAccessExpression everyone = qr.isEmpty() ? createEveryoneExpression(session) : qr.get(0);

		@SuppressWarnings("unchecked")
		List<FakeInstitution> institutions = session.createQuery("FROM Institution").list();
		for( FakeInstitution inst : institutions )
		{
			FakeAccessEntry aclEntry = new FakeAccessEntry();
			aclEntry.grantRevoke = SecurityConstants.GRANT;
			aclEntry.privilege = ActivationConstants.VIEW_LINKED_PORTIONS;
			aclEntry.targetObject = SecurityConstants.TARGET_EVERYTHING;
			aclEntry.aclPriority = SecurityConstants.PRIORITY_ALL_COLLECTIONS;
			aclEntry.aclOrder = 0;
			aclEntry.expression = everyone;
			aclEntry.institution = inst;
			String aggregateOrdering = String.format("%04d %04d %c",
				(aclEntry.aclPriority + SecurityConstants.PRIORITY_MAX), aclEntry.aclOrder, aclEntry.grantRevoke);
			aclEntry.aggregateOrdering = aggregateOrdering;
			session.save(aclEntry);
			result.incrementStatus();
		}
		session.flush();
		session.clear();
	}

	private FakeAccessExpression createEveryoneExpression(Session session)
	{
		FakeAccessExpression everyone = new FakeAccessExpression();
		everyone.dynamic = false;
		everyone.expression = "*";
		session.save(everyone);
		return everyone;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM Institution");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeAccessEntry.class, FakeAccessExpression.class, FakeInstitution.class};
	}

	@Entity(name = "AccessEntry")
	@AccessType("field")
	public static class FakeAccessEntry
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "accessEntryExpression")
		FakeAccessExpression expression;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(nullable = false)
		@Index(name = "accessEntryInstitution")
		FakeInstitution institution;

		@Column(length = 80)
		@Index(name = "targetObjectIndex")
		String targetObject;

		@Column(length = 30)
		@Index(name = "privilegeIndex")
		String privilege;

		@Column(length = 12, nullable = false)
		@Index(name = "aggregateOrderingIndex")
		String aggregateOrdering;

		char grantRevoke;
		int aclOrder;
		int aclPriority;
	}

	@Entity(name = "Institution")
	@AccessType("field")
	public static class FakeInstitution
	{
		@Id
		long id;
	}

	@AccessType("field")
	@Entity(name = "AccessExpression")
	public static class FakeAccessExpression
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		boolean dynamic;
		@Column(length = 1024)
		String expression;
	}
}
