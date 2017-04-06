package com.tle.web.sections.standard.renderers;

import java.io.IOException;
import java.util.Map;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.standard.model.HtmlComponentState;

public abstract class AbstractElementRenderer extends AbstractComponentRenderer
{

	public AbstractElementRenderer(HtmlComponentState state)
	{
		super(state);
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		super.prepareFirstAttributes(writer, attrs);
		attrs.put("name", getName(writer)); //$NON-NLS-1$
	}

	protected String getName(SectionInfo info)
	{
		return state.getName();
	}
}
