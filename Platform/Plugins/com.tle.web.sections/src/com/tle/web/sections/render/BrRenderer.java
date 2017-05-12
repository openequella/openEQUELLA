package com.tle.web.sections.render;

import java.io.IOException;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

public class BrRenderer implements SectionRenderable
{
	@Override
	public void preRender(PreRenderContext info)
	{
		// Nothing to do
	}

	@Override
	@SuppressWarnings("nls")
	public void realRender(SectionWriter writer) throws IOException
	{
		writer.write("<br>");
	}
}
