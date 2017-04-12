package com.tle.web.navigation;

import com.tle.beans.item.Item;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.TagState;

/**
 * @author aholland
 */
public interface BreadcrumbService
{
	String SEARCH_COLLECTION = "searchCollection"; //$NON-NLS-1$
	String VIEW_ITEM = "viewItem"; //$NON-NLS-1$
	String CONTRIBUTE = "contribute"; //$NON-NLS-1$

	TagState getSearchCollectionCrumb(SectionInfo info, String collectionUuid);

	TagState getViewItemCrumb(SectionInfo info, Item item);

	TagState getContributeCrumb(SectionInfo info);
}
