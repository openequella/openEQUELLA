package com.dytech.edge.installer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.tle.common.Check;

public abstract class DatabaseCommand
{
	public static int getDefaultPort(String dbtype)
	{
		if( dbtype.equals("sqlserver") )
		{
			return 1433;
		}
		else if( dbtype.equals("oracle") )
		{
			return 1521;
		}
		else if( dbtype.equals("postgresql") )
		{
			return 5432;
		}
		else
		{
			return 0;
		}
	}

	public static void ensureUnicodeEncoding(Connection conn, String dbtype, String dbname)
		throws NonUnicodeEncodingException, SQLException
	{
		if( dbtype.equals("sqlserver") )
		{
			return;
		}
		else if( dbtype.equals("oracle") )
		{
			ensureOracleEncoding(conn);
		}
		else if( dbtype.equals("postgresql") )
		{
			ensurePostgresqlEncoding(conn, dbname);
		}
	}

	private static void ensureOracleEncoding(Connection conn) throws NonUnicodeEncodingException, SQLException
	{
		final String SQL_PREFIX = "select value from nls_database_parameters where parameter = '";
		final String SQL_POSTFIX = "'";

		checkValueOfFirstColumn(conn, SQL_PREFIX + "NLS_CHARACTERSET" + SQL_POSTFIX, "charset encoding", String.class,
			"UTF8", "AL32UTF8", "AL16UTF16");

		checkValueOfFirstColumn(conn, SQL_PREFIX + "NLS_NCHAR_CHARACTERSET" + SQL_POSTFIX, "nchar charset encoding",
			String.class, "UTF8", "AL32UTF8", "AL16UTF16");
	}

	private static void ensurePostgresqlEncoding(Connection conn, String dbname) throws NonUnicodeEncodingException,
		SQLException
	{
		checkValueOfFirstColumn(conn, "select encoding from pg_database where datname = '" + dbname + "'", "encoding",
			Integer.class, 6);
	}

	private static <T> void checkValueOfFirstColumn(Connection conn, String sql, String messagePrefix, Class<T> klass,
		T... expectedValues) throws SQLException, NonUnicodeEncodingException
	{
		T encoding = klass.cast(getValueOfFirstColumn(conn, sql));
		for( T expectedValue : expectedValues )
		{
			if( Check.bothNullOrEqual(encoding, expectedValue) )
			{
				return;
			}
		}
		throw new NonUnicodeEncodingException(messagePrefix + " value is set to " + encoding);
	}

	@SuppressWarnings("unchecked")
	private static <T> T getValueOfFirstColumn(Connection conn, String sql) throws SQLException,
		NonUnicodeEncodingException
	{
		Statement s = null;
		ResultSet rs = null;
		try
		{
			s = conn.createStatement();
			rs = s.executeQuery(sql);
			if( rs.next() )
			{
				return (T) rs.getObject(1);
			}
			else
			{
				throw new NonUnicodeEncodingException("no results for query");
			}
		}
		finally
		{
			if( rs != null )
			{
				rs.close();
			}

			if( s != null )
			{
				s.close();
			}
		}
	}

	public static class NonUnicodeEncodingException extends Exception
	{
		public NonUnicodeEncodingException(String additional)
		{
			super("Database does not appear to be encoded with UNICODE or UTF-8"
				+ (additional != null ? ": " + additional : ""));
		}
	}
}
