package com.tle.web.freemarker;

import javax.inject.Inject;

public class BasicFreemarkerFactory extends AbstractFreemarkerFactory
{

	@Inject
	public void setConfiguration(BasicConfiguration basicConfiguration)
	{
		this.configuration = basicConfiguration;
	}
}
