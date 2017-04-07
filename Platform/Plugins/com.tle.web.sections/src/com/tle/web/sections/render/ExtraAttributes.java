package com.tle.web.sections.render;

import java.util.HashMap;
import java.util.Map;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

public class ExtraAttributes implements TagProcessor
{
	private Map<String, String> extras = new HashMap<String, String>();

	public ExtraAttributes(String... attrs)
	{
		for( int i = 0; i < attrs.length; i++ )
		{
			extras.put(attrs[i++], attrs[i]);
		}
	}

	@Override
	public void processAttributes(SectionWriter writer, Map<String, String> attrs)
	{
		attrs.putAll(extras);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// nothing
	}

}
