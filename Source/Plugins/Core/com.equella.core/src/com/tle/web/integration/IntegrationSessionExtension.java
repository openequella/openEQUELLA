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

import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.IItem;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionSession;

@NonNullByDefault
public interface IntegrationSessionExtension
{
	void setupSession(SectionInfo info, SelectionSession session, SingleSignonForm form);

	void processResultForSingle(SectionInfo info, SelectionSession session, Map<String, String> params, String prefix,
		IItem<?> item, SelectedResource resource);

	void processResultForMultiple(SectionInfo info, SelectionSession session, ObjectNode link, IItem<?> item,
		SelectedResource resource);
}
