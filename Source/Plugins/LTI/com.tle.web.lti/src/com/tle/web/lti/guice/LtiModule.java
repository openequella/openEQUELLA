package com.tle.web.lti.guice;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.lti.usermanagement.LtiWrapperExtension;
import com.tle.web.sections.equella.guice.SectionsModule;

public class LtiModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		install(new LtiTrackerModule());
	}

	private static class LtiTrackerModule extends PluginTrackerModule
	{
		@Override
		protected void configure()
		{
			bindTracker(LtiWrapperExtension.class, "ltiWrapperExtension", "bean").orderByParameter("order");
		}
	}

}
