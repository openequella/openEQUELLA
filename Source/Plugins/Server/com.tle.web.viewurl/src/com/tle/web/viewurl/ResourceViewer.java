package com.tle.web.viewurl;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;

@NonNullByDefault
public interface ResourceViewer
{
	boolean supports(SectionInfo info, ViewableResource resource);

	@Nullable
	ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog);

	LinkTagRenderer createLinkRenderer(SectionInfo info, ViewableResource resource, Bookmark viewUrl);

	LinkTagRenderer createLinkRenderer(SectionInfo info, ViewableResource resource);

	ViewItemUrl createViewItemUrl(SectionInfo info, ViewableResource resource);

	Bookmark createStreamUrl(SectionInfo info, ViewableResource resource);

	@Nullable
	ViewItemViewer getViewer(SectionInfo info, ViewItemResource resource);
}
