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

package com.tle.core.portal.migration.v64;

import java.io.Serializable;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

/**
 * @author Aaron
 *
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class DenyGuestPortletCreationMigration extends AbstractHibernateDataMigration
{
	private static final Logger LOGGER = Logger.getLogger(DenyGuestPortletCreationMigration.class);

	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(DenyGuestPortletCreationMigration.class)
		+ ".";
	private static final String LOGGED_IN_USER_ROLE_EXPRESSION = SecurityConstants.getRecipient(Recipient.ROLE,
		SecurityConstants.LOGGED_IN_USER_ROLE_ID);
	private static final String EVERYONE_EXPRESSION = SecurityConstants.getRecipient(Recipient.EVERYONE);

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migration.denyguestportletcreation.title");
	}

	private void createAccessExpressionP(Session session, FakeAccessExpression expr)
	{
		FakeAccessExpressionExpressionP ae = new FakeAccessExpressionExpressionP();
		ae.accessExpressionId = expr.id;
		ae.element = expr.expression.trim();
		session.save(ae);

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		LOGGER.debug("Running com.tle.core.portal.migration.v64.DenyGuestPortletCreationMigration");
		FakeAccessExpression expr = null;

		final List<FakeAccessEntry> qr = session
			.createQuery("FROM AccessEntry where privilege = 'CREATE_PORTLET' and targetObject = '*'").list();
		LOGGER.debug("Found " + qr.size() + " potential CREATE_PORTLET privs to convert");
		for( FakeAccessEntry entry : qr )
		{
			LOGGER.debug("Entry expression is " + entry.expression.expression);
			if( entry.expression.expression.trim().equals(EVERYONE_EXPRESSION) )
			{
				LOGGER.debug("Entry matches Everyone expression, converting to " + LOGGED_IN_USER_ROLE_EXPRESSION);
				if( expr == null )
				{
					expr = getExpression(session);
				}
				entry.expression = expr;
				createAccessExpressionP(session, expr);
				session.save(entry);
			}
		}
		session.flush();
		session.clear();
	}

	@SuppressWarnings("unchecked")
	private FakeAccessExpression getExpression(Session session)
	{
		final List<FakeAccessExpression> list = session
			.createQuery("FROM AccessExpression where expression = :expression")
			.setParameter("expression", LOGGED_IN_USER_ROLE_EXPRESSION).list();
		if( list.size() == 0 )
		{
			LOGGER.debug("Making a new expression for logged-in-user");
			final FakeAccessExpression expr = new FakeAccessExpression();
			expr.dynamic = false;
			expr.expression = LOGGED_IN_USER_ROLE_EXPRESSION;
			session.save(expr);
			return expr;
		}
		else
		{
			LOGGER.debug("Using pre-existing expression for logged-in-user");
		}
		return list.get(0);
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM AccessEntry where privilege = 'CREATE_PORTLET' and targetObject = '*'");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeInstitution.class, FakeAccessEntry.class, FakeAccessExpression.class,
				FakeAccessExpressionExpressionP.class};
	}

	@Entity(name = "Institution")
	@AccessType("field")
	public static class FakeInstitution
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
		@ManyToOne(fetch = FetchType.EAGER)
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
}
