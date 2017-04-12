package com.tle.web.myresources;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.web.search.actions.AbstractFavouriteSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;

@Bind
public class MyResourcesFavouriteSearchSection extends AbstractFavouriteSearchSection
{
	@TreeLookup
	private MyResourcesSearchTypeSection myResourcesSearchTypeSection;

	@Override
	protected String getWithin(SectionInfo info)
	{
		MyResourcesSubSearch subSearch = myResourcesSearchTypeSection.getSearchType().getSelectedValue(info);
		return CurrentLocale.get(subSearch.getNameKey());
	}

	@Override
	protected String getCriteria(SectionInfo info)
	{
		return null;
	}
}
