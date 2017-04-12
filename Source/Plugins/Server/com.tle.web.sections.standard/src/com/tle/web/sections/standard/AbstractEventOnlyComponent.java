package com.tle.web.sections.standard;

import com.tle.web.sections.standard.model.HtmlComponentState;

public abstract class AbstractEventOnlyComponent<S extends HtmlComponentState> extends AbstractDisablerComponent<S>
{

	public AbstractEventOnlyComponent(String defaultType)
	{
		super(defaultType);
	}
}
