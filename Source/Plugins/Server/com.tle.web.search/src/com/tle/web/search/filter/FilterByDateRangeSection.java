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

package com.tle.web.search.filter;

import java.util.Date;

import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class FilterByDateRangeSection extends AbstractFilterByDateRangeSection<FreetextSearchEvent>
{
	@PlugKey("filter.bydate.title")
	private static Label LABEL_TITLE;

	@SuppressWarnings("nls")
	@Override
	protected String[] getParameterNames()
	{
		return new String[]{"dp", "ds", "dr"};
	}

	@Override
	public String getAjaxDiv()
	{
		return "date-range-filter"; //$NON-NLS-1$
	}

	@Override
	public Label getTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		Date[] dateRange = getDateRange(info);
		if( dateRange != null )
		{
			event.filterByDateRange(dateRange);
		}
	}

}
