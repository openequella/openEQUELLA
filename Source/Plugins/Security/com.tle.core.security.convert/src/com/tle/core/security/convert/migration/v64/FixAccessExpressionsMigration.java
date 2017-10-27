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

import static com.tle.common.security.SecurityConstants.Recipient.EVERYONE;
import static com.tle.common.security.SecurityConstants.Recipient.HTTP_REFERRER;
import static com.tle.common.security.SecurityConstants.Recipient.IP_ADDRESS;
import static com.tle.common.security.SecurityConstants.Recipient.OWNER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
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

import com.tle.common.Utils;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

/**
 * For the XML migration equivalent, see AclConverter
 * 
 * @author Aaron
 *
 */
@Bind
@Singleton
public class FixAccessExpressionsMigration extends AbstractHibernateDataMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(FixAccessExpressionsMigration.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migration.acl.fixexpressions");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		final List<FakeAccessExpression> exprs = session.createQuery("from AccessExpression").list();

		for( FakeAccessExpression expr : exprs )
		{
			final String e = expr.expression;
			final String[] split = e.split("\\s");
			if( split.length == 1 && e.endsWith(" ") )
			{
				final String trimExpression = Utils.safeSubstring(e, 0, -1);
				// NOTE!  We use 'like' instead of '=' since trailing whitespace is ignored with = (by SQL Server at least)
				final FakeAccessExpression existing = (FakeAccessExpression) session
					.createQuery("from AccessExpression where expression like :expression")
					.setParameter("expression", trimExpression).uniqueResult();
				if( existing != null )
				{
					// check the existing expression... just in case
					final List<String> expressionParts = existing.getExpressionParts();
					if( expressionParts == null || expressionParts.size() == 0 )
					{
						existing.parseExpression();
						session.save(existing);
					}

					//re-point all access entries with old expression to new
					final List<FakeAccessEntry> entriesWithCrapExpression = session
						.createQuery("from AccessEntry where expression = :expression").setParameter("expression", expr)
						.list();
					for( FakeAccessEntry entry : entriesWithCrapExpression )
					{
						entry.expression = existing;
						session.save(entry);
						session.flush();
					}
				}
				else
				{
					expr.expression = trimExpression;
					expr.parseExpression();
					session.save(expr);
					session.flush();
				}
			}
		}
		session.clear();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM AccessExpression");
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
		private static final List<String> OPERATORS = Arrays.asList("not", "or", "and");
		private static final List<Recipient> SET_AS_EVERYONE = Arrays.asList(IP_ADDRESS, HTTP_REFERRER);

		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
		boolean dynamic;
		@Column(length = 1024)
		String expression;
		@ElementCollection(fetch = FetchType.LAZY)
		@CollectionTable(name = "access_expression_expression_p", joinColumns = @JoinColumn(name = "access_expression_id") )
		@Column(name = "element")
		List<String> expressionParts;

		void parseExpression()
		{
			if( expressionParts == null )
			{
				expressionParts = new ArrayList<String>();
			}
			else
			{
				expressionParts.clear();
			}

			for( String part : expression.split("\\s") )
			{
				if( !OPERATORS.contains(part.toLowerCase()) )
				{
					Recipient type = SecurityConstants.getRecipientType(part);

					if( type == OWNER )
					{
						dynamic = true;
					}

					if( SET_AS_EVERYONE.contains(type) )
					{
						expressionParts.add(SecurityConstants.getRecipient(EVERYONE));
					}
					else
					{
						expressionParts.add(part);
					}
				}
			}
		}

		List<String> getExpressionParts()
		{
			return expressionParts;
		}
	}

}
