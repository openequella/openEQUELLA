package com.tle.web.sections.render;

import java.io.IOException;
import java.util.Map;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSStatements;

public class PreRenderOnly implements SectionRenderable, JSStatements, TagProcessor
{
	private PreRenderable prerender;

	public PreRenderOnly()
	{
		// nothing
	}

	@Override
	public void processAttributes(SectionWriter writer, Map<String, String> attrs)
	{
		// nothing
	}

	public PreRenderOnly(PreRenderable prerender)
	{
		this.prerender = prerender;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(prerender);
	}

	@Override
	public String getStatements(RenderContext info)
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		// nothing
	}

}