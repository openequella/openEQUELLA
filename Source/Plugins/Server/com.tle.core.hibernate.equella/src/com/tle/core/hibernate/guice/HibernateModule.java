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
