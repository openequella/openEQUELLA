package com.tle.core.system.service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.tle.beans.DatabaseSchema;
import com.tle.core.hibernate.DataSourceHolder;

/**
 * @author Nicholas Read
 */
public interface SchemaDataSourceService
{
	DataSourceHolder getDataSourceForId(long schemaId);

	DataSourceHolder getReportingDataSourceForId(long schemaId);

	void removeFromCache(long schemaId);

	<V> V executeWithSchema(long schemaId, Callable<V> code);

	<V> Future<V> executeWithSchema(ExecutorService executor, long schemaId, Callable<V> code);

	/**
	 * Return connection details, taking into account the useSystem flag.
	 * 
	 * @param schema
	 * @return Element[0] = url, Element[1] = username, Element[2] = password
	 */
	String[] getConnectionDetails(DatabaseSchema schema);

	void removeSchemaDataSource(long schemaId);
}