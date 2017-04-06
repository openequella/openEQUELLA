package com.tle.web.favourites;

import javax.inject.Inject;
import javax.inject.Named;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.layout.ContentLayout;

@SuppressWarnings("nls")
@Bind
public class RootFavouritesSection extends AbstractRootFavouritesSection
{
	public static final String SEARCH_TREE_NAME = "searchTree";
	public static final String ITEM_TREE_NAME = "itemTree";

	@Inject
	@Named(SEARCH_TREE_NAME)
	private SectionTree searchTree;
	@Inject
	@Named(ITEM_TREE_NAME)
	private SectionTree itemTree;

	@Override
	protected SectionTree getSearchTree()
	{
		return searchTree;
	}

	@Override
	protected SectionTree getItemTree()
	{
		return itemTree;
	}

	@Override
	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return selectionService.getCurrentSession(info) != null ? super.getDefaultLayout(info)
			: ContentLayout.ONE_COLUMN;
	}
}
