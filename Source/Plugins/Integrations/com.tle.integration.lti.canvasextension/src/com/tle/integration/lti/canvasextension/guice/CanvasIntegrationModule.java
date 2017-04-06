package com.tle.integration.lti.canvasextension.guice;

import com.google.inject.name.Names;
import com.tle.integration.lti.canvasextension.CanvasSignon;
import com.tle.web.sections.equella.guice.SectionsModule;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class CanvasIntegrationModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/canvassignon")).toProvider(node(CanvasSignon.class));
	}
}
