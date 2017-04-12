package com.tle.core.connectors.dao;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;

import com.tle.common.connectors.entity.Connector;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;

/**
 * @author aholland
 */
@Bind(ConnectorDao.class)
@Singleton
public class ConnectorDaoImpl extends AbstractEntityDaoImpl<Connector> implements ConnectorDao
{
	public ConnectorDaoImpl()
	{
		super(Connector.class);
	}

	@Override
	public List<Connector> enumerateForUrl(final String url)
	{
		return enumerateAll(new EnabledCallback(true)
		{
			@Override
			public String getAdditionalWhere()
			{
				return super.getAdditionalWhere() + " AND be.serverUrl like :serverUrl";
			}

			@Override
			public void processQuery(Query query)
			{
				super.processQuery(query);
				query.setParameter("serverUrl", url + "%");
			}
		});
	}
}
