package com.tle.web.sections.equella.render;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.sections.equella.component.model.NavBarState;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.TagRenderer;

public class NavBarRenderer extends TagRenderer
{
	public NavBarRenderer(FreemarkerFactory view, NavBarState state)
	{
		super("div", state);
		nestedRenderable = view.createResultWithModel("component/navbar.ftl", state);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(Bootstrap.CSS);
	}
}
