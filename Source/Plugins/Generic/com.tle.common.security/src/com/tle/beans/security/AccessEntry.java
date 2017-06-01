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

package com.tle.beans.security;

import java.util.Date;

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
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;

import com.tle.beans.Institution;
import com.tle.common.security.SecurityConstants;

@Entity
@AccessType("field")
@NamedQueries({
		@NamedQuery(name = "getPrivileges", cacheable = true, readOnly = true, query = ""
			+ "SELECT MAX(ae.aggregateOrdering), ae.privilege, ae.targetObject"
			+ " FROM AccessEntry ae WHERE ae.institution = :institution"
			+ " AND ae.privilege IN (:privileges) AND ae.expression.id IN (:expressions)"
			+ " GROUP BY ae.targetObject, ae.privilege, ae.aggregateOrdering" + " ORDER BY ae.aggregateOrdering DESC"),
		@NamedQuery(name = "getPrivilegesForTargets", cacheable = true, readOnly = true, query = ""
			+ "SELECT MAX(ae.aggregateOrdering), ae.privilege, ae.targetObject"
			+ " FROM AccessEntry ae WHERE ae.institution = :institution"
			+ " AND ae.privilege IN (:privileges) AND ae.targetObject in (:targets)"
			+ " AND ae.expression.id IN (:expressions)"
			+ " GROUP BY ae.targetObject, ae.privilege, ae.aggregateOrdering" + " ORDER BY ae.aggregateOrdering DESC"),
		@NamedQuery(name = "getTargetListEntries", cacheable = true, readOnly = true, query = ""
			+ "SELECT new com.tle.common.security.TargetListEntry(ae.grantRevoke,"
			+ " ae.aclPriority, ae.privilege, ae.expression.expression) FROM AccessEntry ae"
			+ " WHERE ae.institution = :institution AND ae.targetObject = :target"
			+ " AND ae.aclPriority in (:priorities) ORDER BY ae.aclOrder DESC"),
		@NamedQuery(name = "getAllEntries", cacheable = true, readOnly = true, query = ""
			+ "SELECT new com.tle.beans.security.ACLEntryMapping(ae.expression.id,"
			+ " ae.grantRevoke, ae.aclPriority, ae.targetObject, ae.expression.expression)"
			+ " FROM AccessEntry ae WHERE ae.institution = :institution AND"
			+ " ae.targetObject in (:targets) AND ae.privilege in (:privileges)"
			+ " ORDER BY ae.aggregateOrdering DESC"),
		@NamedQuery(name = "getAllEntriesForInstitution", cacheable = false, readOnly = true, query = ""
			+ "SELECT ae FROM AccessEntry ae WHERE ae.institution = :institution")})
public class AccessEntry implements Cloneable
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "accessEntryExpression")
	private AccessExpression expression;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	@Index(name = "accessEntryInstitution")
	private Institution institution;

	@Column(length = 80)
	@Index(name = "targetObjectIndex")
	private String targetObject;

	@Column(length = 30)
	@Index(name = "privilegeIndex")
	private String privilege;

	@Column(length = 12, nullable = false)
	@Index(name = "aggregateOrderingIndex")
	private String aggregateOrdering;

	private char grantRevoke;
	private int aclOrder;
	private int aclPriority;

	private Date expiry;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getPrivilege()
	{
		return privilege;
	}

	public void setPrivilege(String privilege)
	{
		this.privilege = privilege;
	}

	public AccessExpression getExpression()
	{
		return expression;
	}

	public void setExpression(AccessExpression expression)
	{
		this.expression = expression;
	}

	public String getTargetObject()
	{
		return targetObject;
	}

	public void setTargetObject(String targetObject)
	{
		if( targetObject != null && targetObject.contains("null") ) //$NON-NLS-1$
		{
			throw new IllegalArgumentException("Target cannot contain 'null': " + targetObject); //$NON-NLS-1$
		}
		this.targetObject = targetObject;
	}

	public char isGrantRevoke()
	{
		return grantRevoke;
	}

	public void setGrantRevoke(char grantRevoke)
	{
		this.grantRevoke = grantRevoke;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public int getAclOrder()
	{
		return aclOrder;
	}

	public void setAclOrder(int aclOrder)
	{
		this.aclOrder = aclOrder;
	}

	public int getAclPriority()
	{
		return aclPriority;
	}

	public void setAclPriority(int aclPriority)
	{
		this.aclPriority = aclPriority;
	}

	public Date getExpiry()
	{
		return expiry;
	}

	public void setExpiry(Date expiry)
	{
		this.expiry = expiry;
	}

	public String getAggregateOrdering()
	{
		return aggregateOrdering;
	}

	public void setAggregateOrdering(String aggregateOrdering)
	{
		if( aggregateOrdering.length() < 5 )
		{
			throw new IllegalArgumentException("Must be 5 chars or longer"); //$NON-NLS-1$
		}
		this.aggregateOrdering = aggregateOrdering;
	}

	public void generateAggregateOrdering()
	{
		aggregateOrdering = String.format("%04d %04d %c", //$NON-NLS-1$
			(aclPriority + SecurityConstants.PRIORITY_MAX), aclOrder, grantRevoke);
	}

	// Explicit catch of CloneNotSupportedException from super.clone()
	@Override
	public Object clone() // NOSONAR
	{
		try
		{
			return super.clone();
		}
		catch( CloneNotSupportedException e )
		{
			throw new RuntimeException(e);
		}
	}
}
