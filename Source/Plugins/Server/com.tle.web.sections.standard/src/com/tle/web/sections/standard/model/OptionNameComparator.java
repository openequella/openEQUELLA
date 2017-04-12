package com.tle.web.sections.standard.model;

import com.dytech.common.text.NumberStringComparator;

public class OptionNameComparator extends NumberStringComparator<Option<?>>
{
	private static final long serialVersionUID = 1L;

	public static final OptionNameComparator INSTANCE = new OptionNameComparator();

	public OptionNameComparator()
	{
		setCaseInsensitive(true);
	}

	@Override
	public String convertToString(Option<?> t)
	{
		return t.getName();
	}
}
