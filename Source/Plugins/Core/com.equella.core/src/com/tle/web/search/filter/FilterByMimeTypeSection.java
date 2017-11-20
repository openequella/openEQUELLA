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

import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.NameValue;
import com.tle.common.settings.standard.SearchSettings.SearchFilter;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;

public class FilterByMimeTypeSection extends AbstractFilterByMimeTypeSection<FreetextSearchEvent>
{
	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
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
				event.filterByTerms(false, FreeTextQuery.FIELD_ATTACHMENT_MIME_TYPES, allTypes);
			}
		}
	}
}
