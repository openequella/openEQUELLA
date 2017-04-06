package com.tle.web.sections.standard;

import com.tle.web.sections.standard.model.HtmlComponentState;

/**
 * @author Aaron
 */
public class Div extends AbstractRenderedComponent<HtmlComponentState>
{
	public Div()
	{
		super(RendererConstants.DIV);
	}

	@Override
	public Class<HtmlComponentState> getModelClass()
	{
		return HtmlComponentState.class;
	}
}
