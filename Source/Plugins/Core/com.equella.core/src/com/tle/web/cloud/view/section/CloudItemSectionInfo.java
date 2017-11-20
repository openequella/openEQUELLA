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

package com.tle.web.cloud.view.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemKey;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

/**
 * @author Aaron
 */
@NonNullByDefault
public class CloudItemSectionInfo
{
	private final CloudViewableItem viewableItem;

	public CloudItemSectionInfo(CloudViewableItem viewableItem)
	{
		this.viewableItem = viewableItem;
	}

	public CloudViewableItem getViewableItem()
	{
		return viewableItem;
	}

	public ItemKey getItemId()
	{
		return viewableItem.getItemId();
	}

	public static CloudItemSectionInfo getItemInfo(SectionInfo info)
	{
		CloudItemSectionInfo iinfo = info.getAttribute(CloudItemSectionInfo.class);
		if( iinfo == null )
		{
			CloudItemSectionInfoFactory factory = info.lookupSection(CloudItemSectionInfoFactory.class);
			iinfo = factory.createCloudItemSectionInfo(info);
			info.setAttribute(CloudItemSectionInfo.class, iinfo);
		}
		return iinfo;
	}

	@TreeIndexed
	public interface CloudItemSectionInfoFactory extends SectionId
	{
		CloudItemSectionInfo createCloudItemSectionInfo(SectionInfo info);
	}
}
