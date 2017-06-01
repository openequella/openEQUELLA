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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.HibernateException;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;
import org.hibernate.util.SerializationHelper;

public class HibernateCsvType implements UserType
{
	private final int sqlType;

	public HibernateCsvType(int sqlType)
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
		return Collection.class;
	}

	// We inherit this method, so won't quibble about its name
	@Override
	public boolean equals(Object x, Object y) throws HibernateException // NOSONAR
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
			return Arrays.equals(toBytes(x), toBytes(y));
		}
	}

	@Override
	public int hashCode(Object x) throws HibernateException
	{
		return Arrays.hashCode(toBytes(x));
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws SQLException
	{
		String name = names[0];
		String clob = rs.getString(name);
		if( clob == null )
		{
			return null;
		}

		List<Object> list = new ArrayList<Object>();
		StringTokenizer stok = new StringTokenizer(clob, ","); //$NON-NLS-1$
		String type = stok.nextToken();
		while( stok.hasMoreTokens() )
		{
			Object obj = null;
			String str = stok.nextToken();
			if( type.equals("long") ) //$NON-NLS-1$
			{
				obj = Long.parseLong(str);
			}
			else if( type.equals("int") ) //$NON-NLS-1$
			{
				obj = Integer.parseInt(str);
			}
			else if( type.equals("string") ) //$NON-NLS-1$
			{
				obj = unescapeString(str);
			}
			list.add(obj);
		}
		return list;
	}

	private Object unescapeString(String str)
	{
		String result = null;
		if( !str.equals("\\|") ) //$NON-NLS-1$
		{
			result = str.replaceAll("\\\\\\\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
			result = result.replaceAll("\\\\!", ","); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return result;
	}

	private String escapeString(String str)
	{
		String result = "\\|"; //$NON-NLS-1$ 
		if( str != null )
		{
			result = str.replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
			result = result.replaceAll(",", "\\\\!"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return result;
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index) throws SQLException
	{
		String res = null;
		StringBuilder sbuf = new StringBuilder();
		Collection<?> col = (Collection<?>) value;
		boolean first = true;
		if( col != null )
		{
			for( Object object : col )
			{
				if( first )
				{
					if( object instanceof Integer )
					{
						sbuf.append("int"); //$NON-NLS-1$
					}
					if( object instanceof Long )
					{
						sbuf.append("long");//$NON-NLS-1$
					}
					if( object instanceof String )
					{
						sbuf.append("string");//$NON-NLS-1$
					}
					first = false;
				}
				sbuf.append(',');
				if( object instanceof String )
				{
					object = escapeString((String) object);
				}
				sbuf.append(object.toString());
			}

			if( first )
			{
				sbuf.append("string");//$NON-NLS-1$
			}
			res = sbuf.toString();
		}
		st.setString(index, res);
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException
	{
		if( value == null )
		{
			return null;
		}
		return fromBytes(toBytes(value));
	}

	@Override
	public boolean isMutable()
	{
		return true;
	}

	private static byte[] toBytes(Object object) throws SerializationException
	{
		return SerializationHelper.serialize((Serializable) object);
	}

	private static Object fromBytes(byte[] bytes) throws SerializationException
	{
		return SerializationHelper.deserialize(bytes);
	}

	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException
	{
		return (cached == null) ? null : fromBytes((byte[]) cached);
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException
	{
		return (value == null) ? null : toBytes(value);
	}

	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException
	{
		return deepCopy(original);
	}

}
