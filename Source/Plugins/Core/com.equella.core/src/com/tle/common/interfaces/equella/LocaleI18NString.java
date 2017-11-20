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
