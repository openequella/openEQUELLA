package com.tle.webtests.test.admin.multidb;

import java.net.MalformedURLException;
import java.net.URL;

import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.ant.EquellaWait;
import com.tle.webtests.framework.ant.ServiceControl;

public class ManagerServerControl implements ServerControl
{

	private final ServiceControl serviceControl;
	private final TestConfig testConfig;

	public ManagerServerControl(TestConfig testConfig)
	{
		String serviceUrl = testConfig.getProperty("server.serviceurl");
		String password = testConfig.getProperty("server.servicepassword");
		serviceControl = new ServiceControl();
		serviceControl.setUrl(serviceUrl);
		serviceControl.setPassword(password);
		this.testConfig = testConfig;
	}

	@Override
	public void stop()
	{
		serviceControl.setCommand("stop");
		serviceControl.execute();
	}

	@Override
	public void start()
	{
		try
		{
			serviceControl.setCommand("start");
			serviceControl.execute();
			EquellaWait waiter = new EquellaWait();
			waiter.setMaxwait(60);
			waiter.setUrl(new URL(testConfig.getServerUrl() + "institutions.do"));
			waiter.execute();
		}
		catch( MalformedURLException e )
		{
			throw new RuntimeException(e);
		}
	}

}
