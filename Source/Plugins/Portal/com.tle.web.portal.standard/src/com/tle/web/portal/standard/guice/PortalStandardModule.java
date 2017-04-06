package com.tle.web.portal.standard.guice;

import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.tle.web.freemarker.BasicFreemarkerFactory;
import com.tle.web.portal.standard.renderer.SearchPortletRenderer;
import com.tle.web.sections.equella.guice.SectionsModule;

public class PortalStandardModule extends SectionsModule
{

	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		NodeProvider node = node(PortalSearchWrapper.class);
		node.child(SearchPortletRenderer.class);
		bind(Object.class).annotatedWith(Names.named("com.tle.web.portal.standard.searchSelectionPortal")).toProvider(
			node);
		bind(BasicFreemarkerFactory.class).in(Scopes.SINGLETON);
	}

}
