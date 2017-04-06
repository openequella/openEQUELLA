package com.tle.web.searching.actions;

import com.tle.core.guice.Bind;
import com.tle.web.search.actions.AbstractFavouriteSearchSection;
import com.tle.web.searching.SearchWhereModel.WhereEntry;
import com.tle.web.searching.section.SearchQuerySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;

@Bind
public class CriteriaFavouriteSearchSection extends AbstractFavouriteSearchSection
{
	@TreeLookup
	private SearchQuerySection sqs;

	@Override
	protected String getCriteria(SectionInfo info)
	{
		return sqs.getCriteriaText(info);
	}

	@Override
	protected String getWithin(SectionInfo info)
	{
		WhereEntry entry = sqs.getCollectionList().getSelectedValue(info);
		if( entry != null )
		{
			return entry.convert().getName();
		}
		return null;
	}
}
