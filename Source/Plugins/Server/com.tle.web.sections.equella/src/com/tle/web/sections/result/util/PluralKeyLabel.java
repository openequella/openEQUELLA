package com.tle.web.sections.result.util;

public class PluralKeyLabel extends KeyLabel
{

	@SuppressWarnings("nls")
	public PluralKeyLabel(String key, long count)
	{
		super(key + ((count == 1) ? ".1" : ""), count);
	}

}
