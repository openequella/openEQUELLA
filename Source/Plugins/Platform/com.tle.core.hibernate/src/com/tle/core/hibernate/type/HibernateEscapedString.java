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

package com.tle.core.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * This class allows strings to be stored in an escaped form, so that they will
 * never be automatically converted to NULL values by the database, should they
 * be empty. Note that this class will not allow you to use NULL value strings
 * when they are not allowed by Hibernate (such as in Maps). Version for
 * Hibernate 3 that does not add quotes to non-empty strings but escapes by a
 * keyword. This seems more economic in cases where empty strings are rare.
 * 
 * @author Erik Visser, Chess-iT B.V.
 * @author Mika Goeckel, cyber:con gmbh
 */
public class HibernateEscapedString implements UserType
{
	public static final String MARK_EMPTY = "<EmptyString/>"; //$NON-NLS-1$
	private final int sqlType;

	public HibernateEscapedString(int sqlType)
	{
		this.sqlType = sqlType;
	}

	@Override
	public int[] sqlTypes()
	{
		return new int[]{sqlType};
	}

	@Override
	public Class<?> returnedClass()
	{
		return String.class;
	}

	// We inherit this method, so won't quibble about its name
	@Override
	public boolean equals(Object x, Object y) // NOSONAR
	{
		if( x == y ) // NOSONAR
		{
			return true;
		}
		else if( x == null || y == null )
		{
			return false;
		}
		else
		{
			return x.equals(y);
		}
	}

	@Override
	public Object deepCopy(Object x)
	{
		return x;
	}

	@Override
	public boolean isMutable()
	{
		return false;
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws SQLException
	{
		String dbValue = Hibernate.STRING.nullSafeGet(rs, names[0]);
		if( dbValue != null )
		{
			return unescape(dbValue);
		}
		else
		{
			return null;
		}
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index) throws SQLException
	{
		String v = value == null ? null : escape((String) value);
		Hibernate.STRING.nullSafeSet(st, v, index);
	}

	@Override
	public Object assemble(Serializable arg0, Object arg1) throws HibernateException
	{
		return deepCopy(arg0);
	}

	@Override
	public Serializable disassemble(Object value)
	{
		return (Serializable) deepCopy(value);
	}

	/**
	 * Escape a string by quoting the string.
	 */
	private String escape(String string)
	{
		return ((string == null) || (string.length() == 0)) ? MARK_EMPTY : string;
	}

	/**
	 * Unescape by removing the quotes
	 */
	private Object unescape(String string) throws HibernateException
	{
		return (string == null) || (MARK_EMPTY.equals(string)) ? "" : string; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc) (at) see org (dot)
	 * hibernate.usertype.UserType#hashCode(java.lang.Object)
	 */
	@Override
	public int hashCode(Object arg0) throws HibernateException
	{
		return arg0.hashCode();
	}

	/*
	 * (non-Javadoc) (at) see org (dot)
	 * hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public Object replace(Object arg0, Object arg1, Object arg2) throws HibernateException
	{
		return deepCopy(arg0);
	}
}
