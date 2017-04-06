package com.tle.web.selection.filter;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.generic.CachedData.CacheFiller;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;

/**
 * @author aholland
 */
@Bind
@Singleton
public class SelectionAllowedMimeTypes implements CacheFiller<Collection<String>>
{
	@Inject
	private SelectionService selectionService;

	@Override
	public Collection<String> get(SectionInfo info)
	{
		SelectionSession session = selectionService.getCurrentSession(info);
		if( session != null )
		{
			SelectionFilter mimeFilter = (SelectionFilter) session.getAttribute(SelectionFilter.class);
			if( mimeFilter != null )
			{
				return mimeFilter.getAllowedMimeTypes();
			}
		}
		return null;
	}
}