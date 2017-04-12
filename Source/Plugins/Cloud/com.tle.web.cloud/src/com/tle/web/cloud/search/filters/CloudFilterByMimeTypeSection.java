package com.tle.web.cloud.search.filters;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.system.SearchSettings.SearchFilter;
import com.tle.common.NameValue;
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
