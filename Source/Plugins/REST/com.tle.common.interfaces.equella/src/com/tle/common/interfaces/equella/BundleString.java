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

import java.util.Collection;
import java.util.Collections;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.interfaces.I18NString;
import com.tle.common.interfaces.I18NStrings;
import com.tle.common.interfaces.SimpleI18NString;

public class BundleString implements I18NString
{
	protected final Object bundle;
	protected I18NString defaultLabel;
	protected String defaultString = ""; //$NON-NLS-1$
	protected Collection<?> values;

	public BundleString(Object bundle)
	{
		this(bundle, (String) null);
	}

	public BundleString(Object bundle, String defaultString)
	{
		this.bundle = bundle;
		setDefaultString(defaultString);
	}

	public BundleString(Object bundle, I18NString defaultLabel)
	{
		this.bundle = bundle;
		this.defaultLabel = defaultLabel;
		this.defaultString = null;
	}

	public void setDefaultString(String defaultString)
	{
		this.defaultString = defaultString;
	}

	@Override
	public String toString()
	{
		String text = TextBundle.getLocalString(bundle, values, defaultString);
		if( text == null )
		{
			text = (defaultLabel == null ? "" : defaultLabel.toString()); //$NON-NLS-1$
		}
		return text;
	}

	public static I18NString getString(LanguageBundle bundle)
	{
		if( bundle == null )
		{
			return null;
		}
		return new SimpleI18NStrings(bundle.getStrings()).asI18NString(null);
	}

	public static I18NString getString(LanguageBundle bundle, String defaultString)
	{
		if( bundle == null )
		{
			return new SimpleI18NString(defaultString);
		}
		return new SimpleI18NStrings(bundle.getStrings()).asI18NString(defaultString);
	}

	public static I18NStrings getStrings(LanguageBundle bundle)
	{
		if( bundle == null )
		{
			return new SimpleI18NStrings(Collections.<String, LanguageString> emptyMap());
		}
		return new SimpleI18NStrings(bundle.getStrings());
	}
}
