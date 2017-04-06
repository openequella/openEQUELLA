package com.tle.web.viewitem.guice;

import com.tle.core.config.guice.PropertiesModule;
import com.tle.web.sections.equella.guice.SectionsModule;

public class ViewItemModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		install(new ViewItemPropsModule());
	}

	public static class ViewItemPropsModule extends PropertiesModule
	{
		@Override
		protected void configure()
		{
			bindProp("audit.level"); //$NON-NLS-1$
		}

		@Override
		protected String getFilename()
		{

			return "/plugins/com.tle.web.viewitem/mandatory.properties"; //$NON-NLS-1$
		}
	}
}
