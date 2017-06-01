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

package com.tle.web.viewitem.section;

import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ItemSectionInfo.ItemSectionInfoFactory;

public final class ParentViewItemSectionUtils
{
	public static ItemSectionInfo getItemInfo(SectionInfo info)
	{
		ItemSectionInfo iinfo = info.getAttribute(ItemSectionInfo.class);
		if( iinfo == null )
		{
			ItemSectionInfoFactory factory = info.lookupSection(ItemSectionInfoFactory.class);
			iinfo = factory.getItemSectionInfo(info);
			info.setAttribute(ItemSectionInfo.class, iinfo);
		}
		return iinfo;
	}

	public static boolean isForPreview(SectionInfo info)
	{
		RootItemFileSection section = info.lookupSection(RootItemFileSection.class);
		return section.isForPreview(info);
	}

	public static boolean isInIntegration(SectionInfo info)
	{
		RootItemFileSection section = info.lookupSection(RootItemFileSection.class);
		return section.isInIntegration(info);
	}

	private ParentViewItemSectionUtils()
	{
		throw new Error();
	}
}
