package com.tle.blackboard.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import blackboard.db.ConnectionManager;
import blackboard.db.DbUtil;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
// @NonNullByDefault
public class SqlUtil
{
	private static final String TABLE = "equellacontent";

	public static StringBuilder select(String... columns)
	{
		final StringBuilder sql = new StringBuilder("SELECT ");
		if( columns.length == 0 )
		{
			sql.append("*");
		}
		else
		{
			boolean first = true;
			for( String col : columns )
			{
				if( !first )
				{
					sql.append(", ");
				}
				sql.append(col);
				first = false;
			}
		}
		return sql.append(" ").append(from());
	}

	public static StringBuilder update(String... columns)
	{
		final StringBuilder sql = new StringBuilder("UPDATE ").append(table()).append("SET ");
		boolean first = true;
		for( String col : columns )
		{
			if( !first )
			{
				sql.append(", ");
			}
			sql.append(col);
			sql.append("=?");
			first = false;
		}
		return sql.append(" ");
	}

	public static StringBuilder insert(String... columns)
	{
		StringBuilder sql = new StringBuilder("INSERT INTO ").append(table()).append("(");
		boolean first = true;
		for( String col : columns )
		{
			if( !first )
			{
				sql.append(", ");
			}
			sql.append(col);
			first = false;
		}
		return sql.append(") ");
	}

	public static boolean columnExists(String column)
	{
		try
		{
			boolean oracle = DbUtil.safeGetBbDatabase().isOracle();
			SqlUtil.runSql("SELECT " + (oracle ? "" : "TOP 1 ") + " " + column + " FROM " + table(oracle)
				+ (oracle ? " WHERE rownum < 1" : ""), null);
			return true;
		}
		catch( Exception e )
		{
			// debug("Failed reading column "+ column +" from DB");
			return false;
		}
	}

	public static String delete()
	{
		return "DELETE " + from();
	}

	public static String from()
	{
		return "FROM " + table();
	}

	public static String table(boolean oracle)
	{
		return getSchema(oracle) + TABLE + " ";
	}

	public static String table()
	{
		return getSchema() + TABLE + " ";
	}

	public static String getSchema()
	{
		return getSchema(DbUtil.safeGetBbDatabase().isOracle());
	}

	public static String getSchema(boolean oracle)
	{
		return (oracle ? "" : "dbo.");
	}

	public static <T> List<T> runSql(String sql, /* @Nullable */ResultProcessor<T> processor, Object... params)
	{
		PreparedStatement stmt = null;
		List<T> result = null;

		BbUtil.sqlTrace("Getting connection manager");
		ConnectionManager connMgr = DbUtil.safeGetBbDatabase().getConnectionManager();

		Connection conn = null;
		try
		{
			BbUtil.sqlTrace("Getting connection");
			conn = connMgr.getConnection();

			BbUtil.sqlTrace("Creating statement");
			stmt = conn.prepareStatement(sql);

			BbUtil.sqlTrace("Has " + params.length + " params");
			int index = 1;
			for( Object param : params )
			{
				if( param instanceof OptionalParam )
				{
					final OptionalParam<?> opt = (OptionalParam<?>) param;
					if( opt.isUsed() )
					{
						setParam(stmt, index++, opt.getValue());
					}
				}
				else
				{
					setParam(stmt, index++, param);
				}
			}

			BbUtil.sqlTrace("Executing: " + sql);
			if( processor != null )
			{
				result = processor.getResults(stmt.executeQuery());
			}
			else
			{
				stmt.execute();
				result = (List<T>) Collections.singletonList(stmt.getUpdateCount());
			}
			BbUtil.sqlTrace("Success!!");
			return result;
		}
		catch( Exception e )
		{
			BbUtil.error("Failed to runSql", e);
			throw new RuntimeException(e);
		}
		finally
		{
			BbUtil.sqlTrace("Closing statement");
			DbUtil.closeStatement(stmt);
			BbUtil.sqlTrace("Releasing connection");
			ConnectionManager.releaseDefaultConnection(conn);
		}
	}

	public static void setParam(PreparedStatement stmt, int index, /* @Nullable */Object param) throws SQLException
	{
		if( param instanceof String )
		{
			BbUtil.sqlTrace("Setting param string[" + index + "] = " + param);
			stmt.setString(index, (String) param);
		}
		else if( param instanceof Integer )
		{
			BbUtil.sqlTrace("Setting param int[" + index + "] = " + param);
			stmt.setInt(index, (Integer) param);
		}
		else if( param instanceof Timestamp )
		{
			BbUtil.sqlTrace("Setting param timestamp[" + index + "] = " + param);
			stmt.setTimestamp(index, (Timestamp) param);
		}
		else if( param instanceof Boolean )
		{
			boolean pval = (Boolean) param;
			BbUtil.sqlTrace("Setting param boolean[" + index + "] = " + pval);
			stmt.setInt(index, pval ? 1 : 0);
		}
		else if( param == null )
		{
			BbUtil.sqlTrace("Setting param ?[" + index + "] = null");
			stmt.setString(index, null);
		}
		else
		{
			throw new RuntimeException("Parameter " + index + " is an unhandled type: " + param.getClass().getName());
		}
	}

	public interface ResultProcessor<T>
	{
		List<T> getResults(ResultSet results) throws SQLException;
	}

	public static class OptionalParam<T>
	{
		private final T value;
		private final boolean used;

		public OptionalParam(T value, boolean used)
		{
			this.value = value;
			this.used = used;
		}

		public T getValue()
		{
			return value;
		}

		public boolean isUsed()
		{
			return used;
		}
	}
}
