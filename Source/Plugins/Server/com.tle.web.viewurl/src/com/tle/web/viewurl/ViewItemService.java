package com.tle.web.viewurl;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.NameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.viewurl.ViewItemServiceImpl.CachedTree;

/**
 * @author aholland
 */
@NonNullByDefault
public interface ViewItemService
{
	List<NameValue> getViewerNames();

	String getViewerNameKey(String viewerId);

	String getViewerLinkKey(String viewerId);

	@Nullable
	ResourceViewer getViewer(String viewerId);

	List<NameValue> getEnabledViewers(SectionInfo info, ViewableResource resource);

	LinkTagRenderer getViewableLink(SectionInfo info, ViewableResource resource, String viewerId);

	String getDefaultViewerId(ViewableResource resource);

	String getDefaultViewerId(String mimeType);

	@Nullable
	CachedTree getCachedTree(ItemDefinition collection);

	void putCachedTree(ItemDefinition collectionId, CachedTree cachedTree);
}