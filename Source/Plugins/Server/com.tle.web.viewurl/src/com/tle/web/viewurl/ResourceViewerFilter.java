package com.tle.web.viewurl;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;

public interface ResourceViewerFilter
{
	LinkTagRenderer filterLink(SectionInfo info, LinkTagRenderer linkTag, ResourceViewer viewer,
		ViewableResource resource);
}
