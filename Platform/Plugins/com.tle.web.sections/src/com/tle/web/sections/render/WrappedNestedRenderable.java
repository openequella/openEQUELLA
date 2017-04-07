package com.tle.web.sections.render;

import java.io.IOException;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

public class WrappedNestedRenderable implements NestedRenderable
{
	protected NestedRenderable nested;

	public WrappedNestedRenderable(NestedRenderable nested)
	{
		this.nested = nested;
	}

	@Override
	public SectionRenderable getNestedRenderable()
	{
		return nested.getNestedRenderable();
	}

	@Override
	public NestedRenderable setNestedRenderable(SectionRenderable nested)
	{
		this.nested.setNestedRenderable(nested);
		return this;
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		nested.realRender(writer);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		nested.preRender(info);
	}

}
