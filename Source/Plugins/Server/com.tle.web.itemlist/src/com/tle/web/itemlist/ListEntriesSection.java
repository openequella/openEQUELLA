package com.tle.web.itemlist;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.HtmlRenderer;

public interface ListEntriesSection<T extends ListEntry> extends SectionId, HtmlRenderer
{
	void addListItem(SectionInfo info, T item);
}
