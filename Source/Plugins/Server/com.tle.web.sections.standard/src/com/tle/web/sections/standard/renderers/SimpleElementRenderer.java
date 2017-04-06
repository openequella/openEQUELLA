package com.tle.web.sections.standard.renderers;

import com.tle.web.sections.standard.model.HtmlComponentState;

/**
 * Use for DIVs and SPANs
 * 
 * @author aholland
 */
public class SimpleElementRenderer extends AbstractElementRenderer
{
	protected String htmlTag;

	public SimpleElementRenderer(String htmlTag, HtmlComponentState state)
	{
		super(state);
		this.htmlTag = htmlTag;
	}

	@Override
	protected String getTag()
	{
		return htmlTag;
	}
}
