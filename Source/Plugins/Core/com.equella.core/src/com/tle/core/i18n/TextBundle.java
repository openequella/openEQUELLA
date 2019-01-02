/*
 * Copyright 2019 Apereo
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

package com.tle.core.i18n;

import java.util.Collection;
import java.util.Map;

import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.CurrentLocale;

public final class TextBundle
{
	private TextBundle()
	{
		throw new Error();
	}

	public static String getLocalString(Object origObj, BundleCache bundleCache, Collection<?> values,
		String defaultString)
	{
		String output;
		Object obj = origObj;
		if( obj instanceof LanguageBundle )
		{
			obj = ((LanguageBundle) obj).getId();
		}
		if( obj instanceof Long )
		{
			Map<Long, String> bundleMap = bundleCache.getBundleMap();
			output = bundleMap.get(obj);
			if( output == null )
			{
				if( bundleMap.containsKey(obj) )
				{
					// This indicates that the string is empty
					return defaultString;
				}
				else if( origObj instanceof LanguageBundle )
				{
					output = CurrentLocale.get((LanguageBundle) origObj, defaultString);
				}
				else
				{
					return "???no_cached_string for " + origObj + "???"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		else
		{
			if( obj == null )
			{
				return defaultString;
			}
			output = CurrentLocale.get(obj.toString(), toArray(values));
		}
		return output;
	}

	private static Object[] toArray(Collection<?> collection)
	{
		return collection == null ? new Object[0] : collection.toArray();
	}

}
