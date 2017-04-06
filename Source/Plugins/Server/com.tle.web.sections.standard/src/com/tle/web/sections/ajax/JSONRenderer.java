package com.tle.web.sections.ajax;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.SectionRenderable;

public class JSONRenderer implements SectionRenderable
{
	private static ObjectMapper mapper;
	private final Object object;
	private final boolean setContentType;

	static
	{
		mapper = new ObjectMapper();
	}

	public JSONRenderer(Object object, boolean setContentType)
	{
		Class<? extends Object> clazz = object.getClass();
		// Sonar may complain about boolean expression > 3, but this is as
		// expressive as required
		if( clazz == String.class || clazz == Boolean.class || clazz == Integer.class || clazz == Long.class // NOSONAR
			|| clazz == Byte.class || clazz == Character.class )
		{
			object = new SimpleValue(object);
		}
		this.object = object;
		this.setContentType = setContentType;
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		mapper.writeValue(writer, object);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		if( setContentType )
		{
			info.getResponse().setContentType("application/json"); //$NON-NLS-1$
		}
	}

	public static class SimpleValue
	{
		private final Object value;

		public SimpleValue(Object value)
		{
			this.value = value;
		}

		public Object getValue()
		{
			return value;
		}
	}

}
