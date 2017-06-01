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

package com.tle.web.cloud.search.section;

import com.tle.web.cloud.event.CloudSearchEvent;
import com.tle.web.cloud.event.CloudSearchResultsEvent;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.equella.search.PagingSection;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

public class CloudPagingSection extends PagingSection<CloudSearchEvent, CloudSearchResultsEvent>
{

	@Override
	protected SimpleHtmlListModel<PerPageOption> getPerPageListModel()
	{
		return new EnumListModel<PerPageOption>(PER_PAGE_PFX, true, PerPageOption.MIN, PerPageOption.MIDDLE);
	}

}
