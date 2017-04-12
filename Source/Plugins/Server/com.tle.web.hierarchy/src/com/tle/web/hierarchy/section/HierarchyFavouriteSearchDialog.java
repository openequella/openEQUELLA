package com.tle.web.hierarchy.section;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.search.actions.AbstractFavouriteSearchDialog;
import com.tle.web.search.actions.AbstractFavouriteSearchSection;

/**
 * @author Aaron
 */
@Bind
public class HierarchyFavouriteSearchDialog extends AbstractFavouriteSearchDialog
{
	@Inject
	private HierarchyFavouriteSearchSection contentSection;

	@Override
	protected AbstractFavouriteSearchSection getContentSection()
	{
		return contentSection;
	}
}
