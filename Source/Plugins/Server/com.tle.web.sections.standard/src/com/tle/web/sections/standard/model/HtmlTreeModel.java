package com.tle.web.sections.standard.model;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface HtmlTreeModel
{
	List<HtmlTreeNode> getChildNodes(SectionInfo info, @Nullable String xpath);
}
