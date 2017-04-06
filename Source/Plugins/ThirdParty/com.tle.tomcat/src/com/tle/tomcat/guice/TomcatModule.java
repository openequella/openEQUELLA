package com.tle.tomcat.guice;

import com.tle.core.config.guice.MandatoryConfigModule;
import com.tle.core.config.guice.OptionalConfigModule;

@SuppressWarnings("nls")
public class TomcatModule extends MandatoryConfigModule
{
	@Override
	protected void configure()
	{
		bindInt("http.port", -1);
		bindInt("https.port", -1);
		bindInt("ajp.port", -1);
		bindInt("tomcat.max.threads", -2);
		install(new TomcatOptionalModule());
	}

	public static class TomcatOptionalModule extends OptionalConfigModule
	{
		@Override
		protected void configure()
		{
			bindProp("jvmroute.id");
			bindBoolean("tomcat.useBio");
			bindBoolean("sessions.neverPersist");
		}
	}
}
