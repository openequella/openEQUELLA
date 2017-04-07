package com.tle.web.sections.render;

import java.io.Serializable;

public class TextLabel implements Label, Serializable
{
	private static final long serialVersionUID = 1L;

	public static final TextLabel SPACE = new TextLabel(" ", true); //$NON-NLS-1$

	private final String text;
	private final boolean html;

	public TextLabel(String text)
	{
		this(text, false);
	}

	public TextLabel(String text, boolean html)
	{
		this.text = text;
		this.html = html;
	}

	@Override
	public String getText()
	{
		return text;
	}

	@Override
	public boolean isHtml()
	{
		return html;
	}
}
