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

package com.tle.common.remotesqlquerying;

public class ConnectionDetails
{
	private final String driverClass;
	private final String jdbcUrl;
	private final String username;
	private final String password;

	public ConnectionDetails(String driverClass, String jdbcUrl, String username, String password)
	{
		this.driverClass = driverClass;
		this.jdbcUrl = jdbcUrl;
		this.username = username;
		this.password = password;
	}

	public String getDriverClass()
	{
		return driverClass;
	}

	public String getJdbcUrl()
	{
		return jdbcUrl;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}
}
