package com.tle.web.search.filter;

import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.system.SearchSettings.SearchFilter;
import com.tle.common.NameValue;
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
