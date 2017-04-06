package com.tle.web.sections.standard.renderers;

import java.io.IOException;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlComponentState;

public class ComponentRenderer implements SectionRenderable
{
	private SectionRenderable renderable;
	private HtmlComponentState state;

	public ComponentRenderer(SectionRenderable renderable, HtmlComponentState state)
	{
		this.renderable = renderable;
		this.state = state;
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		state.setBeenRendered(true);
		renderable.realRender(writer);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(renderable);
	}

}
