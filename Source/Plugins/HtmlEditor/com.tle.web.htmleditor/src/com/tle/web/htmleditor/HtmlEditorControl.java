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

package com.tle.web.htmleditor;

import java.util.Map;
import java.util.Set;

import com.tle.common.scripting.ScriptContextFactory;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.js.JSDisableable;

/**
 * @author aholland
 */
public interface HtmlEditorControl extends Section, HtmlRenderer, JSDisableable
{
	void setData(SectionInfo info, Map<String, String> properties, boolean restrictedCollections,
		boolean restrictedDynacolls, boolean restrictedSearches, boolean restrictedContributables,
		Map<Class<?>, Set<String>> searchableUuids, Set<String> contributableUuids, String formId,
		ScriptContextFactory scriptContextFactory)
		throws Exception;

	String getHtml(SectionInfo info);

	void setDefaultPropertyName(String defaultPropertyName);
}
