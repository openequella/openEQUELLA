package com.tle.web.hierarchy.guice;

import com.google.inject.name.Names;
import com.tle.web.hierarchy.portlet.renderer.BrowsePortletRenderer;
import com.tle.web.sections.Section;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.selection.home.sections.SelectionPortletRendererWrapper;

@SuppressWarnings("nls")
public class BrowsePortletModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("com.tle.web.hierarchy.portlet.browseSelectionPortal"))
			.toProvider(browsePortletTree());
	}

	private NodeProvider browsePortletTree()
	{
		NodeProvider node = new NodeProvider(SelectionPortletRendererWrapper.class)
		{
			@Override
			protected void customize(Section section)
			{
				SelectionPortletRendererWrapper sprw = (SelectionPortletRendererWrapper) section;
				sprw.setPortletNameKey("com.tle.web.hierarchy.portlet.browse.name");
				sprw.setPortletType("browse");
			}
		};

		node.child(BrowsePortletRenderer.class);

		return node;
	}
}
