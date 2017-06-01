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