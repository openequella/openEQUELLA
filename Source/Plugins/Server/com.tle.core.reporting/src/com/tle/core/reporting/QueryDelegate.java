package com.tle.core.reporting;

import java.util.List;
import java.util.Map;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import com.tle.reporting.IResultSetExt;

/**
 * @author nread
 */
public interface QueryDelegate
{
	Map<String, ?> getDatasourceMetadata() throws OdaException;

	IResultSetExt executeQuery(String query, List<Object> params, int maxRows) throws OdaException;

	IParameterMetaData getParameterMetadata(String query, List<Object> params) throws OdaException;
}
