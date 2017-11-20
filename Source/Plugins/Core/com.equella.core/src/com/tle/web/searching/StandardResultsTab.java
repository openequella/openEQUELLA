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

package com.tle.web.searching;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.selection.section.CourseListSection;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class StandardResultsTab extends AbstractPrototypeSection<StandardResultsTab.StandardResultsTabModel>
	implements
		SearchTab
{
	@PlugKey("results.tab.resultcount")
	private static String KEY_STANDARD_RESULTS_TAB_COUNT;
	private boolean active;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		// TODO: count
		return new LabelRenderer(new PluralKeyLabel(KEY_STANDARD_RESULTS_TAB_COUNT, 0));
	}

	@Override
	public String getId()
	{
		return "standard";
	}

	@Override
	public SectionInfo getForward(SectionInfo info)
	{
		// TODO: this is less hacky than before, but still sub-ottstimal.
		final CourseListSection cls = info.lookupSection(CourseListSection.class);
		if( cls != null && cls.isApplicable(info) )
		{
			return cls.createSearchForward(info);
		}
		return info.createForward("/searching.do");
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new StandardResultsTabModel();
	}

	@Override
	public void setActive()
	{
		active = true;
	}

	@Override
	public boolean isActive()
	{
		return active;
	}

	public static class StandardResultsTabModel
	{
		// Empty, for now
	}
}
