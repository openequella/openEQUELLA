package com.tle.common.i18n;

public class KeyString implements InternalI18NString
{
	private static final long serialVersionUID = 1L;

	private final String key;
	private final Object[] values;

	public KeyString(String key, Object... values)
	{
		this.key = key;
		this.values = values;
	}

	@Override
	public String toString()
	{
		return CurrentLocale.get(key, values);
	}
}
