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

package com.tle.common.taxonomy.datasource.sql;

@SuppressWarnings("nls")
public final class SqlTaxonomyDataSourceConstants
{
	public static final String SQL_DATA_CLASS = "SQL_DATA_CLASS";
	public static final String SQL_JDBC_URL = "SQL_JDBC_URL";
	public static final String SQL_USERNAME = "SQL_USERNAME";
	public static final String SQL_PASSWORD = "SQL_PASSWORD";

	public static final String SQL_RESULT_NAME_TERM = "term";
	public static final String SQL_RESULT_NAME_UUID = "uuid";
	public static final String SQL_RESULT_NAME_DATAKEY = "dataKey";
	public static final String SQL_RESULT_NAME_DATAVALUE = "dataValue";
	public static final String SQL_RESULT_NAME_FULLTERM = "fullterm";
	public static final String SQL_RESULT_NAME_ISLEAF = "isleaf";

	public enum Query
	{
		GET_TERM, GET_CHILD_TERMS, GET_DATA_FOR_TERM, GET_ALL_DATA_FOR_TERM, SEARCH_TERMS_ANY, COUNT_TERMS_ANY,
		SEARCH_TERMS_LEAVES, COUNT_TERMS_LEAVES, SEARCH_TERMS_TOPLEVEL, COUNT_TERMS_TOPLEVEL;
	}

	private SqlTaxonomyDataSourceConstants()
	{
		throw new Error();
	}
}
