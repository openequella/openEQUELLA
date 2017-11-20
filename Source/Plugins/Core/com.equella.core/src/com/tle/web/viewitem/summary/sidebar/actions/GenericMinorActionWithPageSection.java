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

package com.tle.web.viewitem.summary.sidebar.actions;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.render.HideableFromDRMSection;
import com.tle.web.viewitem.summary.section.ItemSummaryContentSection;

@NonNullByDefault
public abstract class GenericMinorActionWithPageSection extends GenericMinorActionSection
	implements
		HideableFromDRMSection
{
	@TreeLookup
	private ItemSummaryContentSection contentSection;

	@Override
	public void execute(SectionInfo info)
	{
		contentSection.setSummaryId(info, getPageSection());
	}

	@Override
	public void showSection(SectionInfo info, boolean show)
	{
		getComponent().setDisplayed(show);
	}

	protected abstract SectionId getPageSection();
}
