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

package com.tle.core.hibernate.guice;

import javax.inject.Inject;
import javax.sql.DataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.DataSourceService;

public class HibernateModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(DataSource.class).toProvider(DataSourceProvider.class).in(Scopes.SINGLETON);
		install(new TransactionModule());
	}

	@Bind
	public static class DataSourceProvider implements Provider<DataSource>
	{
		@Inject
		private DataSourceService dataSourceService;

		@Override
		public DataSource get()
		{
			return dataSourceService.getSystemDataSource().getDataSource();
		}
	}
}
