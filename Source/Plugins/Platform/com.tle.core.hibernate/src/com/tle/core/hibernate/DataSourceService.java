package com.tle.core.hibernate;

public interface DataSourceService
{
	ExtendedDialect getDialect();

	DataSourceHolder getSystemDataSource();

	DataSourceHolder getDataSource(String url, String username, String password);

	String getSystemUrl();

	String getSystemUsername();

	String getSystemPassword();

	String getDriverClass();

	void removeDataSource(String url, String username, String password);
}
