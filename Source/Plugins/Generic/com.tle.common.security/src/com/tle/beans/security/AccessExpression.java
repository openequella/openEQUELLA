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

import static com.tle.common.security.SecurityConstants.Recipient.EVERYONE;
import static com.tle.common.security.SecurityConstants.Recipient.HTTP_REFERRER;
import static com.tle.common.security.SecurityConstants.Recipient.IP_ADDRESS;
import static com.tle.common.security.SecurityConstants.Recipient.OWNER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;

import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;

/**
 * @author Nicholas Read
 */
@Entity
@AccessType("field")
public class AccessExpression implements Cloneable
{
	private static final List<String> OPERATORS = Arrays.asList("not", "or", "and"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final List<Recipient> SET_AS_EVERYONE = Arrays.asList(IP_ADDRESS, HTTP_REFERRER);

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@ElementCollection(fetch = FetchType.LAZY)
	@Column(name = "element")
	private List<String> expressionParts;
	private boolean dynamic;
	@Column(length = 1024)
	private String expression;

	public AccessExpression()
	{
		super();
	}

	public AccessExpression(AccessExpression expr)
	{
		this.id = expr.id;
		this.expressionParts = expr.expressionParts;
		this.dynamic = expr.dynamic;
		this.expression = expr.expression;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public boolean isDynamic()
	{
		return dynamic;
	}

	public void setDynamic(boolean dynamic)
	{
		this.dynamic = dynamic;
	}

	public String getExpression()
	{
		return expression;
	}

	public void setExpression(String expression)
	{
		this.expression = expression;
	}

	public List<String> getExpressionParts()
	{
		return expressionParts;
	}

	public void setExpressionParts(List<String> expressionParts)
	{
		this.expressionParts = expressionParts;
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
