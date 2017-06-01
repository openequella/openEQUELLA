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

package com.tle.hibernate.dialect;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.AssertionFailure;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.util.StringHelper;

/**
 * Extends the ImprovedNamingScheme to make sure that the resulting table name
 * is all lowercase. This helps with Enums on Postgresql.
 * 
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class LowercaseImprovedNamingScheme extends ImprovedNamingStrategy
{
	private static final long serialVersionUID = 1L;
	private final Map<String, String> overrides = new HashMap<String, String>();
	private final Map<String, String> columnOverrides = new HashMap<String, String>();

	public LowercaseImprovedNamingScheme()
	{
		// MySQL5 can't handle schemas and `schemas` doesn't work
		// SQLServer can't handle schema
		registerOverride("schema", "tleschemas");
		registerOverride("comment", "`comment`");

		// SQL Server Specific
		columnOverrides.put("user", "`user`");
		columnOverrides.put("from", "`from`");
		columnOverrides.put("comment", "`comment`");
		columnOverrides.put("date", "`date`");
		columnOverrides.put("schema", "`schema`");
		columnOverrides.put("key", "`key`");
		columnOverrides.put("start", "`start`");
		columnOverrides.put("freetext", "`freetext`");
		columnOverrides.put("order", "`order`");
		columnOverrides.put("comment", "`comment`");
		columnOverrides.put("index", "`index`");

		// Oracle specific
		columnOverrides.put("successful", "`successful`");

		// HSQL Specific
		columnOverrides.put("position", "`position`");

		columnOverrides.put("timestamp", "`timestamp`");
	}

	@Override
	/**
	 * Copied from EJB3NamingStrategy
	 */
	public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName,
		String referencedColumnName)
	{
		String header = propertyName != null ? StringHelper.unqualify(propertyName) : propertyTableName;
		if( header == null )
		{
			throw new AssertionFailure("NammingStrategy not properly filled");
		}
		return columnName(header + "_" + referencedColumnName);
	}

	@Override
	public String propertyToColumnName(String propertyName)
	{
		return super.propertyToColumnName(getColumnName(propertyName));
	}

	@Override
	public String classToTableName(String className)
	{
		return postProcess(super.classToTableName(className));
	}

	@Override
	public String tableName(String tableName)
	{
		return postProcess(super.tableName(tableName));
	}

	@Override
	public String logicalColumnName(String columnName, String propertyName)
	{
		return super.logicalColumnName(getColumnName(columnName), propertyName);
	}

	private String getColumnName(String columnName)
	{
		if( columnName != null )
		{
			String col = columnOverrides.get(StringHelper.unqualify(columnName));
			if( col != null )
			{
				columnName = col;
			}
		}
		if( columnName != null && columnName.length() > 25 )
		{
			columnName = columnName.substring(0, 25);
		}
		return columnName;
	}

	private String postProcess(String tableName)
	{
		tableName = tableName.toLowerCase();
		String result = overrides.get(tableName);
		if( result == null )
		{
			result = tableName;
		}
		if( result != null && result.length() > 30 )
		{
			result = result.substring(0, 30);
		}
		return result;
	}

	private void registerOverride(String from, String to)
	{
		overrides.put(from.toLowerCase(), to.toLowerCase());
	}
}
