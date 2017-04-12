package com.tle.web.sections.standard;

import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.Option;

public class ListOption<T>
{
	private final Option<T> option;
	private final HtmlBooleanState booleanState;

	public ListOption(Option<T> option, HtmlBooleanState state)
	{
		this.option = option;
		this.booleanState = state;
	}

	public Option<T> getOption()
	{
		return option;
	}

	public HtmlBooleanState getBooleanState()
	{
		return booleanState;
	}
}
