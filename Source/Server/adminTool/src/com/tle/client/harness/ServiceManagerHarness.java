/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.client.harness;

import java.io.IOException;
import java.net.URL;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManagerStub;
import javax.jnlp.UnavailableServiceException;

public class ServiceManagerHarness implements ServiceManagerStub
{
	private final URL codebase;
	private final BasicService basic;

	public ServiceManagerHarness(URL codebase)
	{
		this.codebase = codebase;
		basic = new BasicServiceHarness();
	}

	@Override
	public Object lookup(String service) throws UnavailableServiceException
	{
		if( BasicService.class.getName().equals(service) )
		{
			return basic;
		}
		return null;
	}

	@Override
	public String[] getServiceNames()
	{
		return new String[]{BasicService.class.getName()};
	}

	protected class BasicServiceHarness implements BasicService
	{

		@Override
		public URL getCodeBase()
		{
			return codebase;
		}

		@Override
		public boolean isOffline()
		{
			return false;
		}

		@Override
		public boolean showDocument(URL url)
		{
			try
			{
				Runtime.getRuntime().exec("cmd /C start " + url.toString().replaceAll("&", "\"&\""));
			}
			catch( IOException e )
			{
				throw new RuntimeException(e);
			}
			return false;
		}

		@Override
		public boolean isWebBrowserSupported()
		{
			return true;
		}

	}
}
