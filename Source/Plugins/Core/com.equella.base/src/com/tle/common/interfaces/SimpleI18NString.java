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

package com.tle.common.interfaces;

import javax.annotation.Nullable;

public class SimpleI18NString implements I18NString
{
	@Nullable
	private final String text;
	@Nullable
	private final String defaultText;

	public SimpleI18NString(@Nullable String text)
	{
		this.text = text;
		this.defaultText = null;
	}

	public SimpleI18NString(@Nullable String text, @Nullable String defaultText)
	{
		this.text = text;
		this.defaultText = defaultText;
	}

	public SimpleI18NString(@Nullable I18NString text, @Nullable String defaultText)
	{
		this.text = text == null ? null : text.toString();
		this.defaultText = defaultText;
	}

	@Nullable
	@Override
	public String toString()
	{
		if( text == null )
		{
			return defaultText;
		}
		return text;
	}
}
