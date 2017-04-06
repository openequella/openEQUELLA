package com.tle.web.search.actions;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
@Bind
public class StandardFavouriteSearchSection extends AbstractFavouriteSearchSection
{
	@Override
	protected String getWithin(SectionInfo info)
	{
		return null;
	}

	@Override
	protected String getCriteria(SectionInfo info)
	{
		return null;
	}
}
