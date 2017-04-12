package com.tle.web.sections.equella.utils;

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.sections.standard.model.SimpleOption;

public class KeyOption<T> extends SimpleOption<T>
{

	public KeyOption(String name, String value, T obj)
	{
		super(name, value, obj);
	}

	@Override
	public String getName()
	{
		return CurrentLocale.get(super.getName());
	}

}
