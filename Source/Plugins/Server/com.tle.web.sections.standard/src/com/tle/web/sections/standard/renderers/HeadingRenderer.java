package com.tle.web.sections.standard.renderers;

@SuppressWarnings("nls")
public class HeadingRenderer extends DivRenderer
{
	public static HeadingRenderer topLevel(Object text)
	{
		return new HeadingRenderer(2, text);
	}

	public HeadingRenderer(int level, Object text)
	{
		super(text);
		setTag("h" + level);
	}
}
