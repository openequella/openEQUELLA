package com.tle.web.favourites;

import javax.inject.Inject;
import javax.inject.Named;

import com.tle.web.sections.SectionTree;

@SuppressWarnings("nls")
public class RootSkinnyFavouritesSection extends AbstractRootFavouritesSection
{
	public static final String SEARCH_TREE_NAME = "skinnySearchTree";
	public static final String ITEM_TREE_NAME = "skinnyItemTree";

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
}
