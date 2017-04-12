/*
 * Created on 13/04/2006
 */
package com.tle.hibernate.dialect;

import java.sql.Types;

public class MySQL5DialectEQUELLA extends org.hibernate.dialect.MySQL5Dialect
{
	@Override
	protected void registerColumnType(int code, int capacity, String name)
	{
		if( code != Types.CLOB )
		{
			super.registerColumnType(code, capacity, name);
		}
	}
}
