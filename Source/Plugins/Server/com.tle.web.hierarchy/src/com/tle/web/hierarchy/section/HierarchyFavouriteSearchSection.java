package com.tle.web.hierarchy.section;

import com.tle.core.guice.Bind;
import com.tle.web.search.actions.AbstractFavouriteSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;

/**
 * @author Aaron
 */
@Bind
public class HierarchyFavouriteSearchSection extends AbstractFavouriteSearchSection
{
	@TreeLookup
	private TopicDisplaySection topicDisplay;

	@Override
	protected String getWithin(SectionInfo info)
	{
		return topicDisplay.getWithinTopic(info);
	}

	@Override
	protected String getCriteria(SectionInfo info)
	{
		return null;
	}
}
