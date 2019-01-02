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

package com.tle.web.sections.events;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;

/**
 * The event listener interface for participating in the {@code Bookmarking}
 * process.
 * 
 * @see BookmarkEvent
 * @author jmaginnis
 */
@NonNullByDefault
public interface BookmarkEventListener extends TargetedEventListener
{
	void bookmark(SectionInfo info, BookmarkEvent event);

	void document(SectionInfo info, DocumentParamsEvent event);
}
