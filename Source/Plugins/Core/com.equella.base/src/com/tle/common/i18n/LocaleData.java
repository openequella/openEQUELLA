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

package com.tle.common.i18n;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author Nicholas Read
 */
public class LocaleData implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final boolean rightToLeft;
	private final String language;
	private final String country;
	private final String variant;

	// Locale is explicitly rebuilt with getLocale, so the default value (null)
	// is appropriate for deserialization
	private transient Locale locale; // NOSONAR

	public LocaleData(Locale locale, boolean rightToLeft)
	{
		this.locale = locale;
		this.rightToLeft = rightToLeft;

		language = locale.getLanguage();
		country = locale.getCountry();
		variant = locale.getVariant();
	}

	public Locale getLocale()
	{
		if( locale == null )
		{
			locale = new Locale(language, country, variant);
		}
		return locale;
	}

	public boolean isRightToLeft()
	{
		return rightToLeft;
	}
}
