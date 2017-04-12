package com.tle.web.sections.standard.renderers;

import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.model.HtmlComponentState;

@SuppressWarnings("nls")
public class SpanRenderer extends DivRenderer
{
	public SpanRenderer(HtmlComponentState tagState)
	{
		this((TagState) tagState);
	}

	public SpanRenderer(Object innerContent)
	{
		this((String) null, innerContent);
	}

	public SpanRenderer(String styleClass, Object text)
	{
		super(styleClass, text);
		setTag("span");
	}

	public SpanRenderer(TagState tagState)
	{
		super(tagState);
		setTag("span");
	}

	public SpanRenderer(TagState state, Object content)
	{
		super(state, "span", null, content);
	}
}
