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

package com.tle.core.connectors.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.datatools.connectivity.oda.OdaException;

import com.google.common.collect.Maps;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.reporting.SimpleResultSet;
import com.tle.core.reporting.SimpleTypeQuery;
import com.tle.reporting.IResultSetExt;
import com.tle.reporting.MetadataBean;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class ConnectorListQueryDelegate extends SimpleTypeQuery
{
	@Inject
	private ConnectorService connectorService;
	@Inject
	private BundleCache bundleCache;

	@Override
	public Map<String, ?> getDatasourceMetadata() throws OdaException
	{
		return Maps.newHashMap();
	}

	@Override
	public IResultSetExt executeQuery(String query, List<Object> params, int maxRows) throws OdaException
	{
		final List<BaseEntityLabel> connectorLabels = connectorService.listForViewing();

		return convertConnectorList(connectorLabels);
	}

	private IResultSetExt convertConnectorList(List<BaseEntityLabel> connectors)
	{
		List<Object[]> retResults = new ArrayList<Object[]>();

		for( BaseEntityLabel connector : connectors )
		{
			bundleCache.addBundleId(connector.getBundleId());
		}
		Map<Long, String> bundleMap = bundleCache.getBundleMap();

		for( BaseEntityLabel connector : connectors )
		{
			retResults.add(new Object[]{connector.getUuid(), bundleMap.get(connector.getBundleId())});
		}

		MetadataBean bean = new MetadataBean();
		addColumn("uuid", TYPE_STRING, bean); //$NON-NLS-1$
		addColumn("name", TYPE_STRING, bean); //$NON-NLS-1$
		return new SimpleResultSet(retResults, bean);
	}
}
