package com.tle.web.sections.header;

import java.io.IOException;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;

public class BufferedTagRenderer extends TagRenderer
{
	public BufferedTagRenderer(String tag, TagState state)
	{
		super(tag, state);
	}

	private String nested;

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		SectionRenderable renderable = getNestedRenderable();
		nested = SectionUtils.renderToString(writer, renderable);
		super.realRender(writer);
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		writer.write(nested);
	}
}
