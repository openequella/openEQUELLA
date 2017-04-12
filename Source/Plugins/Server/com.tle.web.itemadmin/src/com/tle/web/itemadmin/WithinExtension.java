package com.tle.web.itemadmin;

import java.util.List;

import com.tle.common.search.PresetSearch;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

public interface WithinExtension
{
	void register(String parentId, SectionTree tree);

	void populateModel(SectionInfo info, List<WithinEntry> list);

	PresetSearch createDefaultSearch(SectionInfo info, WithinEntry selected);

	SectionRenderable render(RenderEventContext context);
}
