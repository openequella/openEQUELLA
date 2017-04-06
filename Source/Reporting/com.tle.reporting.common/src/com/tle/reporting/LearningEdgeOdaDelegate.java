package com.tle.reporting;

import java.util.List;
import java.util.Map;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

/**
 * @author nread
 */
public interface LearningEdgeOdaDelegate
{
	String login(String username, String password) throws OdaException;

	Map<String, ?> getDatasourceMetadata(String queryType) throws OdaException;

	IResultSetExt executeQuery(String queryType, String query, List<Object> indexParams, int maxRows)
		throws OdaException;

	IParameterMetaData getParamterMetadata(String queryType, String query, List<Object> indexParams)
		throws OdaException;
}
