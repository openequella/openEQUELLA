package com.tle.web.scripting.advanced.objects.impl;

import java.util.Locale;
import java.util.TimeZone;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.web.scripting.advanced.objects.RegionalScriptObject;
import com.tle.web.scripting.impl.AbstractScriptWrapper;

/**
 * @author aholland
 */
public class RegionalScriptWrapper extends AbstractScriptWrapper implements RegionalScriptObject
{
	@Override
	public Locale getLocale()
	{
		return CurrentLocale.getLocale();
	}

	@Override
	public String getString(String key)
	{
		return CurrentLocale.get(key);
	}

	@Override
	public TimeZone getTimeZone()
	{
		return CurrentTimeZone.get();
	}
}
