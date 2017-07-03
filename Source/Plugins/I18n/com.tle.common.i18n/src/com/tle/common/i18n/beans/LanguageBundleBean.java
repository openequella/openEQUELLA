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

package com.tle.common.i18n.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.tle.common.Check;

/**
 * @author Aaron
 */
public class LanguageBundleBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private long id;
	private Map<String, LanguageStringBean> strings;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Map<String, LanguageStringBean> getStrings()
	{
		return strings;
	}

	public void setStrings(Map<String, LanguageStringBean> strings)
	{
		this.strings = strings;
	}

	public boolean isEmpty()
	{
		if( strings != null )
		{
			for( LanguageStringBean string : strings.values() )
			{
				if( !Check.isEmpty(string.getText()) )
				{
					return false;
				}
			}
		}
		return true;
	}

	public static LanguageBundleBean clone(LanguageBundleBean bundle)
	{
		if( bundle == null )
		{
			return null;
		}
		final LanguageBundleBean newBundle = new LanguageBundleBean();
		final HashMap<String, LanguageStringBean> langStrings = new HashMap<String, LanguageStringBean>();
		final Map<String, LanguageStringBean> oldStrings = bundle.getStrings();
		for( LanguageStringBean langString : oldStrings.values() )
		{
			final LanguageStringBean newString = new LanguageStringBean();
			newString.setText(langString.getText());
			newString.setLocale(langString.getLocale());
			newString.setPriority(langString.getPriority());
			langStrings.put(newString.getLocale(), newString);
		}
		newBundle.setStrings(langStrings);
		return newBundle;
	}
}
