package com.tle.web.workflow.portal;

import com.tle.common.search.DefaultSearch;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;

public interface TaskListSubsearch
{
	String getIdentifier();

	DefaultSearch getSearch();

	boolean isSecondLevel();

	SectionInfo setupForward(SectionInfo from);

	Label getName();

	String getParentIdentifier();
}
