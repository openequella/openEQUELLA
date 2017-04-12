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
import com.tle.core.reporting.SimpleResultSet;
import com.tle.core.reporting.SimpleTypeQuery;
import com.tle.reporting.IResultSetExt;
import com.tle.reporting.MetadataBean;
import com.tle.web.i18n.BundleCache;

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
