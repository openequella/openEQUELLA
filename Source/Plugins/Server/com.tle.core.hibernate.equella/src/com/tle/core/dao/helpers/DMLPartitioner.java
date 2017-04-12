package com.tle.core.dao.helpers;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Partition up the set of query and execute a DML statement over each
 * partition. This helps to prevent locking problems, especially when whole
 * tables start getting locked.
 */
@SuppressWarnings("nls")
public abstract class DMLPartitioner implements HibernateCallback
{
	private static final int MAX_BATCH_SIZE = 500;

	private final String tableName;
	private final String idColumn;

	public DMLPartitioner(String tableName, String idColumn)
	{
		this.tableName = tableName;
		this.idColumn = idColumn;
	}

	@Override
	public final Object doInHibernate(Session session) throws HibernateException
	{
		return withSession(session);
	}

	public final int withSession(Session session)
	{
		final String idFinderHql = buildIdFinderHql();
		final String dmlHql = buildDmlHql();

		int total = 0;

		long startId = -1;
		long endId = -1;
		while( true )
		{
			// Find the next start and end ID
			startId = endId + 1;

			Query idFinder = session.createQuery(idFinderHql);
			idFinder.setMaxResults(MAX_BATCH_SIZE);
			idFinder.setParameter("startId", startId);
			setWhereParams(idFinder);
			ScrollableResults sr = idFinder.scroll();
			try
			{
				if( sr.last() )
				{
					endId = sr.getLong(0);
				}
				else
				{
					// Nothing more to process
					return total;
				}
			}
			finally
			{
				sr.close();
			}

			// Process rows in our ID range
			Query dml = session.createQuery(dmlHql);
			dml.setParameter("startId", startId);
			dml.setParameter("endId", endId);
			setWhereParams(dml);
			setDmlParams(dml);

			total += dml.executeUpdate();
		}
	}

	private String buildIdFinderHql()
	{
		StringBuilder s = new StringBuilder();
		s.append("SELECT ").append(idColumn).append(" FROM ").append(tableName);
		s.append(" ").append(getWhereClause());
		s.append(" AND ").append(idColumn).append(" >= :startId");
		s.append(" ORDER BY ").append(idColumn).append(" ASC");
		return s.toString();
	}

	private String buildDmlHql()
	{
		StringBuilder s = new StringBuilder();
		s.append(getDmlStart());
		s.append(" ").append(getWhereClause());
		s.append(" AND (").append(idColumn).append(" >= :startId");
		s.append(" AND ").append(idColumn).append(" <= :endId)");
		return s.toString();
	}

	public abstract String getWhereClause();

	public abstract void setWhereParams(Query q);

	public abstract String getDmlStart();

	public abstract void setDmlParams(Query q);
}
