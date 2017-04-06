package com.tle.admin.controls;

import org.java.plugin.Plugin;

import com.tle.admin.Driver;
import com.tle.admin.Driver.ControlsRepositoryCreator;
import com.tle.admin.controls.repository.ControlRepository;

public class ControlsPlugin extends Plugin implements ControlsRepositoryCreator
{
	private Driver driver;

	@Override
	protected void doStart() throws Exception
	{
		this.driver = Driver.instance();
		driver.registerControlRepositoryCreator(this);
	}

	@Override
	protected void doStop() throws Exception
	{
		// nothing
	}

	@Override
	public ControlRepository getControlRepository()
	{
		return new ControlRepositoryImpl(driver.getPluginService());
	}

}
