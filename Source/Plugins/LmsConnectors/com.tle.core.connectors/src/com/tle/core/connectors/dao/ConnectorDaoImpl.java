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

package com.tle.core.connectors.dao;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.Query;

import com.tle.common.connectors.entity.Connector;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
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
