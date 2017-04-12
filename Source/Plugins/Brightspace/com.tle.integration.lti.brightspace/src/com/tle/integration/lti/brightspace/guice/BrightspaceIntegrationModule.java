package com.tle.integration.lti.brightspace.guice;

import com.google.inject.name.Names;
import com.tle.core.config.guice.OptionalConfigModule;
import com.tle.integration.lti.brightspace.BrightspaceSignon;
import com.tle.web.sections.Section;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class BrightspaceIntegrationModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/brightspacenavbar"))
			.toProvider(new BrightspaceSignonNodeProvider(BrightspaceSignon.SESSION_TYPE_NAVBAR));
		bind(Object.class).annotatedWith(Names.named("/brightspacequicklink"))
			.toProvider(new BrightspaceSignonNodeProvider(BrightspaceSignon.SESSION_TYPE_QUICKLINK));
		bind(Object.class).annotatedWith(Names.named("/brightspacecoursebuilder"))
			.toProvider(new BrightspaceSignonNodeProvider(BrightspaceSignon.SESSION_TYPE_COURSEBUILDER));
		bind(Object.class).annotatedWith(Names.named("/brightspaceinsertstuff"))
			.toProvider(new BrightspaceSignonNodeProvider(BrightspaceSignon.SESSION_TYPE_INSERTSTUFF));
		install(new BrightspaceLtiOptionalModule());
	}

	private class BrightspaceSignonNodeProvider extends NodeProvider
	{
		private final String type;

		public BrightspaceSignonNodeProvider(String type)
		{
			super(BrightspaceSignon.class);
			this.type = type;
		}

		@Override
		protected void customize(Section section)
		{
			final BrightspaceSignon bso = (BrightspaceSignon) section;
			bso.setType(type);
		}
	}

	private class BrightspaceLtiOptionalModule extends OptionalConfigModule
	{
		@Override
		protected void configure()
		{
			bindProp("brightspace.parameter.username", "ext_d2l_username");
			bindProp("brightspace.parameter.userid", "ext_d2l_orgdefinedid");
		}
	}
}
