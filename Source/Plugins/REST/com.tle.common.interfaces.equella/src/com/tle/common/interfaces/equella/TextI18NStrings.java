package com.tle.common.interfaces.equella;

import java.util.Collections;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.interfaces.I18NString;
import com.tle.common.interfaces.I18NStrings;
import com.tle.common.interfaces.SimpleI18NString;

/**
 * Use when you need an I18NStrings but only have single text value
 * 
 * @author Aaron
 */
@NonNullByDefault
public class TextI18NStrings implements I18NStrings
{
	@Nullable
	private final String text;

	public TextI18NStrings(@Nullable String text)
	{
		this.text = text;
	}

	@SuppressWarnings("null")
	@Override
	public Map<String, String> getStrings()
	{
		return Collections.singletonMap("", text);
	}

	@Nullable
	@Override
	public I18NString asI18NString(@Nullable String defaultText)
	{
		if( defaultText == null && text == null )
		{
			return null;
		}
		if( text == null )
		{
			return new SimpleI18NString(defaultText);
		}
		return new SimpleI18NString(text);
	}

	@Nullable
	@Override
	public String toString()
	{
		return text;
	}
}