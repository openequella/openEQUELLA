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

package com.tle.core.system.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.tle.beans.DatabaseSchema;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.hibernate.DataSourceHolder;
import com.tle.core.hibernate.DataSourceService;
import com.tle.core.hibernate.SystemDatabase;
import com.tle.core.hibernate.event.SchemaListener;
import com.tle.core.system.dao.DatabaseSchemaDao;
import com.tle.core.system.service.SchemaDataSourceService;

@Singleton
@SystemDatabase
@Bind(SchemaDataSourceService.class)
@SuppressWarnings("nls")
public class SchemaDataSourceServiceImpl implements SchemaDataSourceService, SchemaListener
{
	@Inject
	private DatabaseSchemaDao dao;
	@Inject
	private DataSourceService dataSourceService;

	private final LoadingCache<Long, DataSourceHolder> schemaDatasources = CacheBuilder.newBuilder().build(
		new CacheLoader<Long, DataSourceHolder>()
		{
			@Override
			public DataSourceHolder load(Long id)
			{
				DatabaseSchema schema = getSchemaById(id);
				if( schema == null )
				{
					throw new RuntimeException("No schema for id:" + id);
				}
				if( schema.isUseSystem() )
				{
					return dataSourceService.getSystemDataSource();
				}
				return dataSourceService.getDataSource(schema.getUrl(), schema.getUsername(), schema.getPassword());
			}
		});

	private final LoadingCache<Long, DataSourceHolder> reportingSchemaDatasources = CacheBuilder.newBuilder().build(
		new CacheLoader<Long, DataSourceHolder>()
		{
			@Override
			public DataSourceHolder load(Long id)
			{
				DatabaseSchema schema = getSchemaById(id);
				if( schema == null )
				{
					throw new RuntimeException("No schema for id:" + id);
				}
				String[] connectionDetails = getConnectionDetails(schema);
				return dataSourceService.getDataSource(tryElse(schema.getReportingUrl(), connectionDetails[0]),
					tryElse(schema.getReportingUsername(), connectionDetails[1]),
					tryElse(schema.getReportingPassword(), connectionDetails[2]));
			}

			private String tryElse(String primary, String backup)
			{
				return !Check.isEmpty(primary) ? primary : backup;
			}
		});

	@Override
	public DataSourceHolder getDataSourceForId(long schemaId)
	{
		return schemaDatasources.getUnchecked(schemaId);
	}

	@Override
	public DataSourceHolder getReportingDataSourceForId(long schemaId)
	{
		return reportingSchemaDatasources.getUnchecked(schemaId);
	}

	@Transactional
	protected DatabaseSchema getSchemaById(long id)
	{
		return dao.findById(id);
	}

	@Override
	public <V> V executeWithSchema(long schemaId, Callable<V> code)
	{
		DataSourceHolder dataSource = getDataSourceForId(schemaId);
		DataSourceHolder originalDataSource = CurrentDataSource.get();
		CurrentDataSource.set(dataSource);
		try
		{
			return code.call();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		finally
		{
			CurrentDataSource.set(originalDataSource);
		}
	}

	@Override
	public void systemSchemaUp()
	{
		// tis ok
	}

	@Override
	public void schemasAvailable(Collection<Long> schemas)
	{
		// no need to worry
	}

	@Override
	public void schemasUnavailable(Collection<Long> schemas)
	{
		for( Long schemaId : schemas )
		{
			removeFromCache(schemaId);
		}
	}

	@Override
	public void removeSchemaDataSource(long schemaId)
	{
		DatabaseSchema schema = getSchemaById(schemaId);
		if( !schema.isUseSystem() && !isSystemSchemaEquivalent(schema) )
		{
			try
			{
				((Closeable) getDataSourceForId(schemaId).getDataSource()).close();
			}
			catch( IOException e )
			{
				// Whatevva. I do what I want.
			}
			dataSourceService.removeDataSource(schema.getUrl(), schema.getUsername(), schema.getPassword());
		}
	}

	private boolean isSystemSchemaEquivalent(DatabaseSchema schema)
	{
		return dataSourceService.getSystemUrl().equals(schema.getUrl())
			&& dataSourceService.getSystemUsername().equals(schema.getUsername())
			&& dataSourceService.getSystemPassword().equals(schema.getPassword());
	}

	@Override
	public void removeFromCache(long schemaId)
	{
		schemaDatasources.invalidate(schemaId);
		reportingSchemaDatasources.invalidate(schemaId);
	}

	@Override
	public String[] getConnectionDetails(DatabaseSchema schema)
	{
		if( schema.isUseSystem() )
		{
			return new String[]{dataSourceService.getSystemUrl(), dataSourceService.getSystemUsername(),
					dataSourceService.getSystemPassword()};
		}
		return new String[]{schema.getUrl(), schema.getUsername(), schema.getPassword()};
	}

	@Override
	public <V> Future<V> executeWithSchema(ExecutorService executor, final long schemaId, final Callable<V> code)
	{
		return executor.submit(new Callable<V>()
		{
			@Override
			public V call() throws Exception
			{
				return executeWithSchema(schemaId, code);
			}
		});
	}
}
