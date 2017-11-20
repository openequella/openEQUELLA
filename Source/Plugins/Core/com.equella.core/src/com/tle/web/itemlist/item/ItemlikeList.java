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

package com.tle.web.itemlist.item;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.itemlist.ListEntriesSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;

@NonNullByDefault
public interface ItemlikeList<I extends IItem<?>, LE extends ItemlikeListEntry<I>> extends ListEntriesSection<LE>
{
	LE addItem(SectionInfo info, I item, @Nullable FreetextResult result);

	void setNullItemsRemovedOnModel(SectionInfo info, boolean nullItemsRemoved);

	ListSettings<LE> getListSettings(SectionInfo info);

	List<LE> initEntries(RenderContext context);
}
