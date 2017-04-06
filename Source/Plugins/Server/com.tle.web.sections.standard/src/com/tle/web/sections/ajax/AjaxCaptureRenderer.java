package com.tle.web.sections.ajax;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
public class AjaxCaptureRenderer implements SectionRenderable
{
	private final String divId;
	private final SectionRenderable renderer;
	private final Map<String, Object> params;

	public AjaxCaptureRenderer(String divId, SectionRenderable renderer)
	{
		this(divId, renderer, null);
	}

	public AjaxCaptureRenderer(String divId, SectionRenderable renderer, Map<String, Object> params)
	{
		this.divId = divId;
		this.renderer = renderer;
		this.params = params;
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		AjaxRenderContext ajaxContext = writer.getAttributeForClass(AjaxRenderContext.class);
		if( ajaxContext != null )
		{
			Writer newWriter = ajaxContext.startCapture(writer, divId, params, false);
			if( !newWriter.equals(writer) )
			{
				writer = new SectionWriter(newWriter, writer);
			}
		}
		writer.preRender(renderer);
		renderer.realRender(writer);
		if( ajaxContext != null )
		{
			ajaxContext.endCapture(divId);
		}
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// nothing
	}
}
