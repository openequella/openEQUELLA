package com.tle.common.remotesqlquerying;

public interface RemoteRemoteSqlQueryingService
{
	void testConnection(String driverClass, String jdbcUrl, String username, String password);
}
