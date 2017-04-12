package com.tle.common.interfaces.equella;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.interfaces.I18NString;
import com.tle.common.interfaces.I18NStrings;

public class LocaleI18NString implements I18NString
{
	// FIXME: this bestString is matched by the first users locale that inits
	// it. If this is stored anywhere outside of the current thread it won't
	// work.
	private String bestString;
	private final String defaultText;
	private final I18NStrings strings;

	public LocaleI18NString(I18NStrings strings, String defaultText)
	{
		this.strings = strings;
		this.defaultText = defaultText;
	}

	@Override
	public String toString()
	{
		if( bestString == null )
		{
			bestString = LangUtils.getClosestObjectForLocale(strings.getStrings(), CurrentLocale.getLocale());
			if( Check.isEmpty(bestString) )
			{
				bestString = defaultText;
			}
		}
		return bestString;
	}
}
