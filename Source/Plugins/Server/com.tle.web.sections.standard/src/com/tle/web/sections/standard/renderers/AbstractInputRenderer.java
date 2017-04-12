package com.tle.web.sections.standard.renderers;

import java.io.IOException;
import java.util.Map;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.standard.model.HtmlComponentState;

@SuppressWarnings("nls")
public abstract class AbstractInputRenderer extends AbstractElementRenderer
{
	private final String type;

	public AbstractInputRenderer(HtmlComponentState state, String type)
	{
		super(state);
		this.type = type;
	}

	protected String getType()
	{
		return type;
	}

	@Override
	protected final String getTag()
	{
		return "input";
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		attrs.put("type", getType());
		super.prepareFirstAttributes(writer, attrs);
	}

	@Override
	protected void writeEnd(SectionWriter writer) throws IOException
	{
		// just dont
	}

}
