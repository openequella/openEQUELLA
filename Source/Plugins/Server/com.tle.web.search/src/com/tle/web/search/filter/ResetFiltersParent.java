package com.tle.web.search.filter;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;

@NonNullByDefault
@TreeIndexed
public interface ResetFiltersParent extends SectionId
{
	void addResetDiv(SectionTree tree, List<String> ajaxList);

	ResetFiltersSection<?> getResetFiltersSection();
}
