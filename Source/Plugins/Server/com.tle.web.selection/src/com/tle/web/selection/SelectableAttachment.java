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

package com.tle.web.selection;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface SelectableAttachment
{
	/**
	 * 
	 * @param info
	 * @param item
	 * @param attachmentUuid null in the case of determining if <em>any</em> attachment is selectable
	 * @return
	 */
	boolean isAttachmentSelectable(SectionInfo info, IItem<?> item, @Nullable String attachmentUuid);

	boolean canBePushed(String attachmentUuid);

	boolean isItemCopyrighted(IItem<?> item);

	List<String> getApplicableCourseCodes(String attachmentUuid);
}
