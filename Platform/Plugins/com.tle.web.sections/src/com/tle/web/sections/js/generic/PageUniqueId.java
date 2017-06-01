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

package com.tle.web.sections.js.generic;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.js.ElementId;

public class PageUniqueId implements ElementId
{
	private boolean used;

	@Override
	@SuppressWarnings("nls")
	public String getElementId(SectionInfo info)
	{
		String id = info.getAttribute(this);
		if( id == null )
		{
			String prepend = info.getAttribute(PageUniqueId.class);
			prepend = prepend == null ? "i" : prepend + "_i";
			id = prepend + SectionUtils.getPageUniqueId(info);
			info.setAttribute(this, id);
		}
		return id;
	}

	@Override
	public boolean isElementUsed()
	{
		return used;
	}

	@Override
	public boolean isStaticId()
	{
		return false;
	}

	@Override
	public void registerUse()
	{
		used = true;
	}
}
