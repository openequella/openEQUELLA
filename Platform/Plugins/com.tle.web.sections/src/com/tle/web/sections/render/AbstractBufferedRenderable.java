package com.tle.web.sections.render;

import java.io.IOException;
import java.io.StringWriter;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;

@NonNullByDefault
public abstract class AbstractBufferedRenderable implements SectionRenderable
{
	private boolean rendered;
	private SimpleSectionResult renderedResult;

	@Override
	public void preRender(PreRenderContext info)
	{
		if( !rendered )
		{
			StringWriter out = new StringWriter();
			try
			{
				render(new SectionWriter(out, info));
			}
			catch( IOException e )
			{
				throw new SectionsRuntimeException(e);
			}
			renderedResult = new SimpleSectionResult(out.toString());
			rendered = true;
		}
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		if( !rendered )
		{
			render(writer);
		}
		else
		{
			renderedResult.realRender(writer);
		}

	}

	public abstract void render(SectionWriter writer) throws IOException;

}
