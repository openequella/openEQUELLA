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

package com.tle.core.search.searchset.virtualisation;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.i18n.LangUtils;
import com.tle.common.search.searchset.SearchSet;
import com.tle.core.search.VirtualisableAndValue;

public abstract class VirtualisationHelper<T>
{
	public abstract SearchSet getSearchSet(T obj);

	public abstract T newFromPrototypeForValue(T obj, String value);

	public VirtualisableAndValue<T> newVirtualisedPathFromPrototypeForValue(T obj, String value, int count)
	{
		return new VirtualisableAndValue<T>(newFromPrototypeForValue(obj, value), value, count);
	}

	public LanguageBundle newLanguageBundleForValue(LanguageBundle bundle, String value)
	{
		bundle = LanguageBundle.clone(bundle);
		if( !LangUtils.isEmpty(bundle) )
		{
			for( LanguageString ls : bundle.getStrings().values() )
			{
				ls.setText(modifyString(ls.getText(), value));
			}
		}
		return bundle;
	}

	protected String modifyString(String text, String value)
	{
		return text;
	}
}
