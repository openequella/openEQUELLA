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

package com.tle.web.searching.actions;

import com.tle.core.guice.Bind;
import com.tle.web.search.actions.AbstractFavouriteSearchSection;
import com.tle.web.searching.SearchWhereModel.WhereEntry;
import com.tle.web.searching.section.SearchQuerySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;

@Bind
public class CriteriaFavouriteSearchSection extends AbstractFavouriteSearchSection
{
	@TreeLookup
	private SearchQuerySection sqs;

	@Override
	protected String getCriteria(SectionInfo info)
	{
		return sqs.getCriteriaText(info);
	}

	@Override
	protected String getWithin(SectionInfo info)
	{
		WhereEntry entry = sqs.getCollectionList().getSelectedValue(info);
		if( entry != null )
		{
			return entry.convert().getName();
		}
		return null;
	}
}
