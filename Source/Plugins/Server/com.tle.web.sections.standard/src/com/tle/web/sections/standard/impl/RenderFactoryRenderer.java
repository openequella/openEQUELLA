package com.tle.web.sections.standard.impl;

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.model.HtmlComponentState;

public class RenderFactoryRenderer implements SectionRenderable
{
	private final RendererFactory renderFactory;
	private final HtmlComponentState state;

	private SectionRenderable realRenderer;

	public RenderFactoryRenderer(HtmlComponentState state, RendererFactory renderFactory)
	{
		this.state = Preconditions.checkNotNull(state);
		this.renderFactory = Preconditions.checkNotNull(renderFactory);
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		getRealRenderer(writer).realRender(writer);
	}

	private SectionRenderable getRealRenderer(RenderContext info)
	{
		if( realRenderer == null )
		{
			realRenderer = renderFactory.getRenderer(info, state);
			state.fireRendererCallback(info, realRenderer);
		}
		return realRenderer;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		getRealRenderer(info).preRender(info);
	}

}
