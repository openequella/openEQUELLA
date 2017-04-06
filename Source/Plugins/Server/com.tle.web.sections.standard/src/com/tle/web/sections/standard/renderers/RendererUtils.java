package com.tle.web.sections.standard.renderers;

import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.standard.AbstractRenderedComponent;

public final class RendererUtils
{
	public static String getIdForObject(Object object)
	{
		if( object instanceof ElementId )
		{
			return ((AbstractRenderedComponent<?>) object).getSectionId();
		}
		else if( object instanceof String )
		{
			return (String) object;
		}
		return null;
	}

	private RendererUtils()
	{
		throw new Error();
	}
}
