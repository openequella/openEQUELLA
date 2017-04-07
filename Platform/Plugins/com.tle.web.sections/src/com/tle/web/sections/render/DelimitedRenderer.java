package com.tle.web.sections.render;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

public class DelimitedRenderer implements SectionRenderable
{
	private SectionRenderable delimiter;
	private SectionRenderable[] renderers;

	public DelimitedRenderer(String delimited, Object... objects)
	{
		this(new LabelRenderer(new TextLabel(delimited)), Arrays.asList(objects));
	}

	public DelimitedRenderer(SectionRenderable delimiter, Collection<?> objects)
	{
		this.delimiter = delimiter;
		this.renderers = SectionUtils.convertToRenderers(objects);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(delimiter);
		info.preRender(renderers);
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		boolean first = true;
		for( SectionRenderable renderer : renderers )
		{
			if( !first )
			{
				writer.render(delimiter);
			}
			first = false;
			writer.render(renderer);
		}
	}

}
