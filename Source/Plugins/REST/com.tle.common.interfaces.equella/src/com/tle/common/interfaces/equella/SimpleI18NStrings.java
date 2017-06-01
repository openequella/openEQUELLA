/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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