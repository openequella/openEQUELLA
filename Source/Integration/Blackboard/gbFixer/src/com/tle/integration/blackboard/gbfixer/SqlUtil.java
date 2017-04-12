package com.tle.integration.blackboard.gbfixer;

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

	private static String from()
	{
		return "FROM " + table();
	}

	private static String table()
	{
		return getSchema() + TABLE + " ";
	}

	private static String getSchema()
	{
		return getSchema(DbUtil.safeGetBbDatabase().isOracle());
	}

	private static String getSchema(boolean oracle)
	{
		return (oracle ? "" : "dbo.");
	}

	public static <T> List<T> runSql(String sql, /* @Nullable */ResultProcessor<T> processor, Object... params)
	{
		PreparedStatement stmt = null;
		List<T> result = null;

		ConnectionManager connMgr = DbUtil.safeGetBbDatabase().getConnectionManager();

		Connection conn = null;
		try
		{
			conn = connMgr.getConnection();
			stmt = conn.prepareStatement(sql);

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

			if( processor != null )
			{
				result = processor.getResults(stmt.executeQuery());
			}
			else
			{
				stmt.execute();
				result = (List<T>) Collections.singletonList(stmt.getUpdateCount());
			}
			return result;
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		finally
		{
			DbUtil.closeStatement(stmt);
			ConnectionManager.releaseDefaultConnection(conn);
		}
	}

	private static void setParam(PreparedStatement stmt, int index, /* @Nullable */Object param) throws SQLException
	{
		if( param instanceof String )
		{
			stmt.setString(index, (String) param);
		}
		else if( param instanceof Integer )
		{
			stmt.setInt(index, (Integer) param);
		}
		else if( param instanceof Timestamp )
		{
			stmt.setTimestamp(index, (Timestamp) param);
		}
		else if( param instanceof Boolean )
		{
			boolean pval = (Boolean) param;
			stmt.setInt(index, pval ? 1 : 0);
		}
		else if( param == null )
		{
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
