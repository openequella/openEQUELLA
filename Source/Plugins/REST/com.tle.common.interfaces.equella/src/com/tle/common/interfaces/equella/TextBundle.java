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

import com.tle.beans.entity.LanguageBundle;
import com.tle.common.i18n.CurrentLocale;

public final class TextBundle
{
	private TextBundle()
	{
		throw new Error();
	}

	public static String getLocalString(Object origObj, Collection<?> values, String defaultString)
	{
		String output;
		Object obj = origObj;
		if( obj == null )
		{
			return defaultString;
		}
		LanguageBundle bundle;
		// TODO:???
		// if( origObj instanceof BundleReference )
		// {
		// bundle = ((BundleReference) origObj).getBundle();
		// }
		// else
		// {
		bundle = (LanguageBundle) origObj;
		// }
		output = CurrentLocale.get(bundle);
		return output;
	}
}
