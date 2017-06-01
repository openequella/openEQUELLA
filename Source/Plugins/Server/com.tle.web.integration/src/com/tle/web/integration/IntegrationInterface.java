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

package com.tle.web.integration;

import com.tle.common.NameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.layout.LayoutSelector;
import com.tle.web.selection.SelectionSession;

public interface IntegrationInterface
{
	IntegrationSessionData getData();

	String getClose();

	String getCourseInfoCode();

	NameValue getLocation();

	LayoutSelector createLayoutSelector(SectionInfo info);

	/**
	 * 
	 * @param info
	 * @param session
	 * @return true if you want to maintain selected resources, otherwise false
	 */
	boolean select(SectionInfo info, SelectionSession session);
}
