package com.tle.common.interfaces.equella;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.LanguageString;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.interfaces.I18NString;
import com.tle.common.interfaces.I18NStrings;
import com.tle.common.interfaces.SimpleI18NString;

@NonNullByDefault
public class SimpleI18NStrings implements I18NStrings
{
	private final Map<String, LanguageString> strings;

	public SimpleI18NStrings(Map<String, LanguageString> strings)
	{
		this.strings = strings;
	}

	@Override
	public Map<String, String> getStrings()
	{
		Map<String, String> nameStrings = Maps.newHashMap();
		for( Entry<String, LanguageString> entry : strings.entrySet() )
		{
			nameStrings.put(entry.getKey(), entry.getValue().getText());
		}
		return nameStrings;
	}

	@Nullable
	@Override
	public I18NString asI18NString(@Nullable String defaultText)
	{
		if( defaultText == null && strings.isEmpty() )
		{
			return null;
		}
		final LanguageString bestString = LangUtils.getClosestObjectForLocale(strings, CurrentLocale.getLocale());
		if( bestString == null )
		{
			return new SimpleI18NString(defaultText);
		}
		return new SimpleI18NString(bestString.getText());
	}

	@Nullable
	@Override
	public String toString()
	{
		final I18NString asI18NString = asI18NString(null);
		if( asI18NString != null )
		{
			return asI18NString.toString();
		}
		return null;
	}
}