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

package com.tle.web.cloud.search.filters;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.NameValue;
import com.tle.common.settings.standard.SearchSettings.SearchFilter;
import com.tle.web.cloud.event.CloudSearchEvent;
import com.tle.web.search.filter.AbstractFilterByMimeTypeSection;
import com.tle.web.sections.SectionInfo;

public class CloudFilterByMimeTypeSection extends AbstractFilterByMimeTypeSection<CloudSearchEvent>
{
	@Override
	public void prepareSearch(SectionInfo info, CloudSearchEvent event) throws Exception
	{
		List<NameValue> selectedFilters = mimeTypes.getSelectedValues(info);

		List<String> allTypes = new ArrayList<String>();
		if( !selectedFilters.isEmpty() )
		{
			for( NameValue filterNameVal : selectedFilters )
			{
				final SearchFilter filter = getSearchSettings().getSearchFilter(filterNameVal.getValue());
				// it shouldn't be null, but there is a window where it may not
				// exist any more.
				if( filter != null )
				{
					allTypes.addAll(filter.getMimeTypes());
				}
			}
			if( !allTypes.isEmpty() )
			{
				event.filterByFormats(allTypes);
			}
		}
	}
}
