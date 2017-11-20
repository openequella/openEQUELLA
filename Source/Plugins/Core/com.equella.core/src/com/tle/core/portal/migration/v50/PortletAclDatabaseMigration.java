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

package com.tle.core.portal.migration.v50;

import static com.tle.common.security.SecurityConstants.Recipient.EVERYONE;
import static com.tle.common.security.SecurityConstants.Recipient.HTTP_REFERRER;
import static com.tle.common.security.SecurityConstants.Recipient.IP_ADDRESS;
import static com.tle.common.security.SecurityConstants.Recipient.OWNER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.dytech.edge.common.Constants;
import com.tle.beans.security.ACLEntryMapping;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.common.security.TargetListEntry;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

@Bind
@Singleton
public class PortletAclDatabaseMigration extends AbstractHibernateDataMigration
{
	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM Institution"); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		final Query exprQuery = session.createQuery("FROM AccessExpression WHERE expression = :expression"); //$NON-NLS-1$

		final FakeAccessExpression owner = getOrCreateExpression(session, exprQuery,
			SecurityConstants.getRecipient(Recipient.OWNER));
		final FakeAccessExpression absolutelyEverybodyInTheWholeWideWorld = getOrCreateExpression(session, exprQuery,
			SecurityConstants.getRecipient(Recipient.EVERYONE));

		List<FakeInstitution> institutions = session.createQuery("FROM Institution").list(); //$NON-NLS-1$
		for( FakeInstitution institution : institutions )
		{
			final String target = "*"; //$NON-NLS-1$

			grantPrivilege(session, "CREATE_PORTLET", absolutelyEverybodyInTheWholeWideWorld, target, //$NON-NLS-1$
				institution);

			grantPrivilege(session, "VIEW_PORTLET", owner, target, institution); //$NON-NLS-1$
			grantPrivilege(session, "EDIT_PORTLET", owner, target, institution); //$NON-NLS-1$
			grantPrivilege(session, "DELETE_PORTLET", owner, target, institution); //$NON-NLS-1$
			result.incrementStatus();
		}
	}

	@SuppressWarnings("unchecked")
	private FakeAccessExpression getOrCreateExpression(Session session, Query exprQuery, String expression)
	{
		List<FakeAccessExpression> list = exprQuery.setParameter("expression", expression).list(); //$NON-NLS-1$
		if( !list.isEmpty() )
		{
			return list.get(0);
		}
		else
		{
			FakeAccessExpression result = new FakeAccessExpression();
			result.setExpression(expression);
			result.parseExpression();
			session.save(result);
			return result;
		}
	}

	private void grantPrivilege(Session session, String privilege, FakeAccessExpression expression, String target,
		FakeInstitution institution)
	{
		addEntry(session, privilege, SecurityConstants.GRANT, expression, target, institution);
	}

	private void addEntry(Session session, String privilege, char grantRevoke, FakeAccessExpression expression,
		String target, FakeInstitution institution)
	{
		FakeAccessEntry newEntry = new FakeAccessEntry();
		newEntry.setGrantRevoke(grantRevoke);
		newEntry.setPrivilege(privilege);
		newEntry.setTargetObject(target);
		newEntry.setAclPriority(SecurityConstants.PRIORITY_ALL_PORTLETS);
		newEntry.setAclOrder(0);
		newEntry.setExpression(expression);
		newEntry.setInstitution(institution);
		newEntry.generateAggregateOrdering();

		session.save(newEntry);
		session.flush();
		session.clear();
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeInstitution.class, FakeAccessEntry.class, FakeAccessExpression.class,
				TargetListEntry.class, ACLEntryMapping.class};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.portal.migration.portletacls.title", Constants.BLANK); //$NON-NLS-1$
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

		public void setId(long id)
		{
			this.id = id;
		}

		public void setExpression(FakeAccessExpression expression)
		{
			this.expression = expression;
		}

		public void setInstitution(FakeInstitution institution)
		{
			this.institution = institution;
		}

		public void setTargetObject(String targetObject)
		{
			this.targetObject = targetObject;
		}

		public void setPrivilege(String privilege)
		{
			this.privilege = privilege;
		}

		public void setAggregateOrdering(String aggregateOrdering)
		{
			this.aggregateOrdering = aggregateOrdering;
		}

		public void setGrantRevoke(char grantRevoke)
		{
			this.grantRevoke = grantRevoke;
		}

		public void setAclOrder(int aclOrder)
		{
			this.aclOrder = aclOrder;
		}

		public void setAclPriority(int aclPriority)
		{
			this.aclPriority = aclPriority;
		}

		public void generateAggregateOrdering()
		{
			aggregateOrdering = String.format("%04d %04d %c", //$NON-NLS-1$
				(aclPriority + SecurityConstants.PRIORITY_MAX), aclOrder, grantRevoke);
		}
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
		static final List<String> OPERATORS = Arrays.asList("not", "or", "and");
		static final List<Recipient> SET_AS_EVERYONE = Arrays.asList(IP_ADDRESS, HTTP_REFERRER);

		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		boolean dynamic;

		@Column(length = 1024)
		String expression;

		@ElementCollection(fetch = FetchType.LAZY)
		@Column(name = "element")
		List<String> expressionParts;

		public void setId(long id)
		{
			this.id = id;
		}

		public void setDynamic(boolean dynamic)
		{
			this.dynamic = dynamic;
		}

		public void setExpression(String expression)
		{
			this.expression = expression;
		}

		public void parseExpression()
		{
			if( expressionParts == null )
			{
				expressionParts = new ArrayList<String>();
			}
			else
			{
				expressionParts.clear();
			}

			for( String part : expression.split("\\s") ) //$NON-NLS-1$
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
	}
}
