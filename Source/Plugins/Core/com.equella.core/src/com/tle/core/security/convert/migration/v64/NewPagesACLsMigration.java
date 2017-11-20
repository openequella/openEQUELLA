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

package com.tle.core.security.convert.migration.v64;

import java.io.Serializable;
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
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@SuppressWarnings("nls")
public class NewPagesACLsMigration extends AbstractHibernateDataMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(NewPagesACLsMigration.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migration.v64.acl.newpages");
	}

	private FakeAccessExpression createLoggedInUserExpression(Session session)
	{
		FakeAccessExpression loggedInUser = new FakeAccessExpression();
		
		loggedInUser = new FakeAccessExpression();
		loggedInUser.dynamic = false;
		loggedInUser.expression = "R:TLE_LOGGED_IN_USER_ROLE ";
		session.save(loggedInUser);
		return loggedInUser;
	}
	
	private void createAccessExpressionP(Session session, FakeAccessExpression loggedInUser)
	{
		FakeAccessExpressionExpressionP ae = new FakeAccessExpressionExpressionP();
		ae.accessExpressionId = loggedInUser.id;
		ae.element = loggedInUser.expression.trim();
		session.save(ae);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		List<FakeAccessExpression> qr = session
			.createQuery("from AccessExpression where expression = 'R:TLE_LOGGED_IN_USER_ROLE '").list();
		FakeAccessExpression loggedInUser = qr.isEmpty() ? createLoggedInUserExpression(session) : qr.get(0);
		createAccessExpressionP(session, loggedInUser);

		List<FakeInstitution> institutions = session.createQuery("FROM Institution").list();
		for( FakeInstitution inst : institutions )
		{
			FakeAccessEntry aclEntry = makeAclEntry(loggedInUser, inst, "SEARCH_PAGE");
			session.save(aclEntry);
			result.incrementStatus();

			aclEntry = makeAclEntry(loggedInUser, inst, "DASHBOARD_PAGE");
			session.save(aclEntry);
			result.incrementStatus();

			aclEntry = makeAclEntry(loggedInUser, inst, "HIERARCHY_PAGE");
			session.save(aclEntry);
			result.incrementStatus();
		}
		session.flush();
		session.clear();
	}

	private FakeAccessEntry makeAclEntry(FakeAccessExpression loggedInUser, FakeInstitution inst, String priv)
	{
		FakeAccessEntry aclEntry = new FakeAccessEntry();
		aclEntry.grantRevoke = SecurityConstants.GRANT;
		aclEntry.privilege = priv;
		aclEntry.targetObject = SecurityConstants.TARGET_EVERYTHING;
		aclEntry.aclPriority = -SecurityConstants.PRIORITY_INSTITUTION;
		aclEntry.aclOrder = 0;
		aclEntry.expression = loggedInUser;
		aclEntry.institution = inst;
		String aggregateOrdering = String.format("%04d %04d %c",
			(aclEntry.aclPriority + SecurityConstants.PRIORITY_MAX), aclEntry.aclOrder, aclEntry.grantRevoke);
		aclEntry.aggregateOrdering = aggregateOrdering;
		return aclEntry;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM Institution");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeAccessEntry.class, FakeAccessExpression.class, FakeAccessExpressionExpressionP.class, FakeInstitution.class};
	}

	@Entity(name = "ItemDefinition")
	@AccessType("field")
	public class FakeItemDefinition
	{
		@Id
		long id;
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
	@Entity(name = "AccessExpressionExpression_P")
	public static class FakeAccessExpressionExpressionP implements Serializable
	{
		private static final long serialVersionUID = 1L;
		@Id
		long accessExpressionId;
		@Id
		String element;
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
