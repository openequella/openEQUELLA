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

package com.tle.core.services.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tle.common.Check;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class ProxyDetails
{
	private String host;
	private int port;
	private String username;
	private String password;
	private String exceptions;
	private final Set<String> exceptionsSet = new HashSet<String>();

	public ProxyDetails()
	{
	}

	public ProxyDetails(String host, int port)
	{
		this.host = host;
		this.port = port;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getExceptions()
	{
		return exceptions;
	}

	public void setExceptions(String exceptions)
	{
		this.exceptions = exceptions;
		if( exceptions != null )
		{
			List<String> newExceptions = Arrays.asList(exceptions.split("\\|"));
			for( int i = 0; i < newExceptions.size(); i++ )
			{
				newExceptions.set(i, "^" + newExceptions.get(i).replaceAll("\\*", ".*").replaceAll("\\.", "\\.") + "$");
			}
			exceptionsSet.addAll(newExceptions);
		}
	}

	public boolean isHostExcepted(String testHost)
	{
		for( String ex : exceptionsSet )
		{
			if( testHost.matches(ex) )
			{
				return true;
			}
		}
		return false;
	}

	public boolean isConfigured()
	{
		return !Check.isEmpty(host);
	}
}