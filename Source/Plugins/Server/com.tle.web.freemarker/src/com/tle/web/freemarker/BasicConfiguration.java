package com.tle.web.freemarker;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.DebugSettings;

import freemarker.template.Configuration;

@SuppressWarnings("nls")
@Bind
@Singleton
public class BasicConfiguration extends Configuration
{
	public BasicConfiguration()
	{
		setDateFormat("full");
		setTimeFormat("short");
		setDateTimeFormat("long_short");
		setLocalizedLookup(false);
		setTemplateUpdateDelay(DebugSettings.isDebuggingMode() ? 0 : (int) TimeUnit.DAYS.toSeconds(1));
	}
}
