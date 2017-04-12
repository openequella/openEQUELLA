/*
 * Created on 9/02/2006
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
