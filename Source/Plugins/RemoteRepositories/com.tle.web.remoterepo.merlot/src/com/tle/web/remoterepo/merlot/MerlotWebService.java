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

package com.tle.web.remoterepo.merlot;

import java.util.Collection;

import com.tle.beans.search.MerlotSettings;
import com.tle.common.NameValue;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public interface MerlotWebService
{
	MerlotSettings getSettings(SectionInfo info);

	/**
	 * @param categoryId blank or null for root categories.
	 */
	Collection<NameValue> getCategories(SectionInfo info, String categoryId);

	Collection<NameValue> getCommunities(SectionInfo info);

	Collection<NameValue> getLanguages(SectionInfo info);

	Collection<NameValue> getMaterialTypes(SectionInfo info);

	Collection<NameValue> getTechnicalFormats(SectionInfo info);

	Collection<NameValue> getAudiences(SectionInfo info);

	String lookupLanguage(SectionInfo info, String langCode);
}
