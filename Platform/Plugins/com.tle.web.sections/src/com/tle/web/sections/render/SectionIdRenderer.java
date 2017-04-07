package com.tle.web.sections.render;

import java.io.IOException;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;

public class SectionIdRenderer implements SectionRenderable
{
	private final SectionId id;

	public SectionIdRenderer(SectionId id)
	{
		this.id = id;
	}

	public SectionRenderable getRealRenderer(RenderContext context)
	{
		SectionRenderable renderable = context.getAttribute(this);
		if( renderable == null )
		{
			renderable = SectionUtils.renderSection(context, id);
			context.setAttribute(this, renderable);
		}
		return renderable;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(getRealRenderer(info));
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		SectionRenderable realRenderer = getRealRenderer(writer);
		if( realRenderer != null )
		{
			realRenderer.realRender(writer);
		}
	}

}
