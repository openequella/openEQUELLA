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

package com.tle.mypages.parse.conversion;

import org.ccil.cowan.tagsoup.AttributesImpl;

import com.tle.web.sections.js.JSUtils;

public class RawAnchorReplacementConversion implements HrefConversion
{
	@SuppressWarnings("nls")
	@Override
	public String convert(String href, AttributesImpl atts)
	{
		if( href.startsWith("#") )
		{
			String onclick = "document.location.hash = " + JSUtils.toJSString(href.substring(1)) + "; return false;";
			int index = atts.getIndex("onclick");
			if( index == -1 )
			{
				atts.addAttribute("", "onclick", "onclick", "CDATA", onclick);
			}
			else
			{
				atts.setValue(index, onclick);
			}
		}
		return href;
	}
}
