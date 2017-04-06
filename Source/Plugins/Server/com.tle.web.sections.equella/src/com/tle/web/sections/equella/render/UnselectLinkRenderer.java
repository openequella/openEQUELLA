package com.tle.web.sections.equella.render;

import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;

/**
 * @author Aaron
 */
public class UnselectLinkRenderer extends LinkRenderer
{
	@SuppressWarnings("nls")
	public UnselectLinkRenderer(HtmlLinkState state, Label alt)
	{
		super(state);
		setStyles(null, "unselect", null);
		setTitle(alt);
	}
}
