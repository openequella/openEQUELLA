package com.tle.web.viewitem.section;

import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;

public interface ResourceViewerAware extends ViewItemViewer
{
	void beforeRender(SectionInfo info, ViewItemResource resource);
}
