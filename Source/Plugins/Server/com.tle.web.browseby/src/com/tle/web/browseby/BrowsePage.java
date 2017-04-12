package com.tle.web.browseby;

import com.tle.web.search.base.AbstractRootSearchSection;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.render.Label;

public class BrowsePage extends AbstractRootSearchSection<ContextableSearchSection.Model>
{
	@TreeLookup
	private BrowseSection browseSection;

	@Override
	public Label getTitle(SectionInfo info)
	{
		return browseSection.getTitle(info);
	}
}
