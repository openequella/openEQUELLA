package com.tle.web.sections.render;

import java.io.IOException;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;

public class WrappedTemplateRenderable implements TemplateRenderable
{
	protected SectionRenderable renderable;

	public WrappedTemplateRenderable(SectionRenderable renderable)
	{
		this.renderable = renderable;
	}

	@Override
	public boolean exists(RenderContext context)
	{
		return true;
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		renderable.realRender(writer);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		renderable.preRender(info);
	}

	@Override
	public String toString()
	{
		return renderable.toString();
	}
}
