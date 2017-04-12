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
