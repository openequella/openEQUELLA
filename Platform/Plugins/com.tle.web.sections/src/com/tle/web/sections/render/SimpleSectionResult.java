package com.tle.web.sections.render;

import java.io.IOException;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

@NonNullByDefault
public class SimpleSectionResult implements SectionRenderable
{
	private final Object result;
	private final String responseMimeType;

	public SimpleSectionResult(@Nullable Object result)
	{
		this(result, null);
	}

	public SimpleSectionResult(@Nullable Object result, @Nullable String responseMimeType)
	{
		this.result = (result == null ? "" : result); //$NON-NLS-1$
		this.responseMimeType = responseMimeType;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		if( !Check.isEmpty(responseMimeType) )
		{
			info.getResponse().setContentType(responseMimeType);
		}
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		writer.write(result.toString());
	}

}
