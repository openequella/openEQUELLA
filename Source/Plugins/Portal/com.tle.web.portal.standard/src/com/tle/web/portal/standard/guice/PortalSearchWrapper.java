package com.tle.web.portal.standard.guice;

import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.home.sections.SelectionPortletRendererWrapper;

public class PortalSearchWrapper extends SelectionPortletRendererWrapper
{
	@PlugKey("selection.search.name")
	private static Label LABEL_TITLE;

	public PortalSearchWrapper()
	{
		setPortletType("search"); //$NON-NLS-1$
	}

	@Override
	protected Label getBoxLabel()
	{
		return LABEL_TITLE;
	}
}
