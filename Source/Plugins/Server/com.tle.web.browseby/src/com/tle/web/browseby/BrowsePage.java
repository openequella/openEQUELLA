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

package com.tle.web.browseby;

import com.tle.web.search.base.AbstractRootSearchSection;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.render.Label;

public class BrowsePage extends AbstractRootSearchSection<ContextableSearchSection.Model>
{
	@TreeLookup
	private BrowseSection browseSection;

	@Override
	public Label getTitle(SectionInfo info)
	{
		return browseSection.getTitle(info);
	}
}
