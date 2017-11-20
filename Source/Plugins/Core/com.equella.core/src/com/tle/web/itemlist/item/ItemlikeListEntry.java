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

import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.web.itemlist.ListEntry;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.viewurl.ViewableResource;

/**
 * @author Aaron
 */
public interface ItemlikeListEntry<I extends IItem<?>> extends ListEntry
{
	// This can be null in the case that the item no longer exists
	@Nullable
	I getItem();

	List<ViewableResource> getViewableResources();

	// Debateable...

	TagState getTag();

	Label getSelectLabel();

	Label getUnselectLabel();

	void setToggle(SectionRenderable toggle);

	void addExtras(SectionRenderable extra);

	UnmodifiableAttachments getAttachments();

	void setSelected(boolean selected);

	boolean isSelectable();

	void setSelectable(boolean selectable);
}
