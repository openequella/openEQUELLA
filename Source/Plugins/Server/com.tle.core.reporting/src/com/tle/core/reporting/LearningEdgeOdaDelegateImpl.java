package com.tle.core.reporting;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.reporting.IResultSetExt;
import com.tle.reporting.LearningEdgeOdaDelegate;

@Singleton
@SuppressWarnings("nls")
@Bind(LearningEdgeOdaDelegate.class)
public class LearningEdgeOdaDelegateImpl implements LearningEdgeOdaDelegate
{
	private PluginTracker<QueryDelegate> delegates;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		delegates = new PluginTracker<QueryDelegate>(pluginService, this.getClass(), "queryDelegate", "id");
		delegates.setBeanKey("class");
	}

	@Override
	public String login(String username, String password)
	{
		return "";
	}

	@Override
	public IResultSetExt executeQuery(String queryType, String query, List<Object> params, int maxRows)
		throws OdaException
	{
		// Check.checkNotEmpty(query);
		return getDelegate(queryType).executeQuery(query, params, maxRows);
	}

	@Override
	public IParameterMetaData getParamterMetadata(String queryType, String query, List<Object> params)
		throws OdaException
	{
		// Check.checkNotEmpty(query);
		return getDelegate(queryType).getParameterMetadata(query, params);
	}

	@Override
	public Map<String, ?> getDatasourceMetadata(String queryType) throws OdaException
	{
		return getDelegate(queryType).getDatasourceMetadata();
	}

	private QueryDelegate getDelegate(String type)
	{
		QueryDelegate delegate = delegates.getBeanMap().get(type);
		if( delegate != null )
		{
			return delegate;
		}

		throw new RuntimeException("Implementation for query type '" + type + "' not found");
	}
}
