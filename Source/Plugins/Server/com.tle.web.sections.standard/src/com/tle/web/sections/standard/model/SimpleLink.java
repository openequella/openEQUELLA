package com.tle.web.sections.standard.model;

import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;

public class SimpleLink extends HtmlLinkState
{

	public SimpleLink(String href, Label label, String target)
	{
		setBookmark(new SimpleBookmark(href));
		setLabel(label);
		setTarget(target);
	}

	public SimpleLink(String href, String label, String target)
	{
		this(href, new TextLabel(label), target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends HtmlComponentState> Class<T> getClassForRendering()
	{
		return (Class<T>) HtmlLinkState.class;
	}
}
