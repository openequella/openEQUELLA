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

package com.tle.web.remoterepo.z3950;

import com.tle.core.fedsearch.RemoteRepoSearchResult;
import com.tle.web.remoterepo.event.RemoteRepoSearchResultEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.PagingSection;

/**
 * @author Aaron
 */
public class Z3950PagingSection
	extends
		PagingSection<Z3950SearchEvent, RemoteRepoSearchResultEvent<RemoteRepoSearchResult>>
{
	@Override
	protected int getPerPage(SectionInfo info)
	{
		PerPageOption val = getPerPage().getSelectedValue(info);

		switch( val )
		{
			case MAX:
				return 30;
			case MIDDLE:
				return 20;
			case MIN:
				return 10;
			default:
				return 10;

		}
	}
}
