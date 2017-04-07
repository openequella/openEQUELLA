package com.tle.web.sections.render;

import java.io.IOException;
import java.util.Comparator;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

public class OrderedRenderer implements SectionRenderable
{
	private final int order;
	private final SectionRenderable renderer;

	public OrderedRenderer(int order, SectionRenderable renderer)
	{
		this.order = order;
		this.renderer = renderer;
	}

	public OrderedRenderer(int order, PreRenderable prerenderer)
	{
		this.order = order;
		if( prerenderer instanceof SectionRenderable )
		{
			this.renderer = (SectionRenderable) prerenderer;
		}
		else
		{
			this.renderer = new PreRenderOnly(prerenderer);
		}
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		renderer.preRender(info);
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		renderer.realRender(writer);
	}

	public int getOrder()
	{
		return order;
	}

	public static class RendererOrder implements Comparator<OrderedRenderer>
	{
		@Override
		public int compare(OrderedRenderer o1, OrderedRenderer o2)
		{
			return o1.getOrder() - o2.getOrder();
		}
	}
}