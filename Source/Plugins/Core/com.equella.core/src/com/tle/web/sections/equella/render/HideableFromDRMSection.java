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

package com.tle.web.sections.equella.render;

import com.tle.web.sections.SectionInfo;

/**
 * An interface for sections which can be instructed to hide or unhide
 * themselves. This facility is referred to when rendering the DRMFilterSection.
 * If the DRMFilterSection is rendered in its 'Accept/Reject/Preview mode, then
 * almost all actions that otherwise appear in the RHS actions column are
 * hidden. The exception is the SearchPrevNextSection. This provides the ability
 * to click,click,click through a sequence of items in a search result list,
 * viewing either the item summary, or the DRM filter screen where appropriate,
 * and in both cases to have the Next Prev buttons rendered and in the same
 * location on screen.<br>
 * Sections implementing this interface require a Model class which implements
 * HideableFromDRMModel
 * 
 * @author larry
 */
public interface HideableFromDRMSection
{
	void showSection(SectionInfo info, boolean show);
}
