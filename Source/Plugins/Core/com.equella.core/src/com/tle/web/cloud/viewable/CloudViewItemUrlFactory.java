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

package com.tle.web.cloud.viewable;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface CloudViewItemUrlFactory
{
	CloudViewItemUrl createItemUrl(SectionInfo info, CloudViewableItem viewableItem);

	CloudViewItemUrl createItemUrl(SectionInfo info, CloudViewableItem viewableItem, int flags);

	CloudViewItemUrl createItemUrl(SectionInfo info, CloudViewableItem viewableItem, CloudAttachment cloudAttachment);

	CloudViewItemUrl createItemUrl(SectionInfo info, CloudViewableItem viewableItem, CloudAttachment cloudAttachment,
		int flags);
}
