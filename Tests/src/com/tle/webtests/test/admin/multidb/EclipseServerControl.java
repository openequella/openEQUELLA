package com.tle.webtests.test.admin.multidb;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JOptionPane;

import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.ant.EquellaWait;

public class EclipseServerControl implements ServerControl
{
	private final TestConfig testConfig;

	public EclipseServerControl(TestConfig testConfig)
	{
		this.testConfig = testConfig;
	}

	@Override
	public void stop()
	{
		JOptionPane.showMessageDialog(null, "Stop the server");
	}

	@Override
	public void start()
	{
		try
		{
			JOptionPane.showMessageDialog(null, "Start the server");
			EquellaWait waiter = new EquellaWait();
			waiter.setMaxwait(60);
			waiter.setUrl(new URL(testConfig.getAdminUrl() + "institutions.do"));
			waiter.execute();
		}
		catch( MalformedURLException mue )
		{
			throw new RuntimeException(mue);
		}
	}

}
