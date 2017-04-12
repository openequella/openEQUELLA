package com.tle.web.searching.actions;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.search.actions.AbstractFavouriteSearchDialog;

@Bind
public class CriteriaFavouriteSearchDialog extends AbstractFavouriteSearchDialog
{
	@Inject
	private CriteriaFavouriteSearchSection contentSection;

	@Override
	public CriteriaFavouriteSearchSection getContentSection()
	{
		return contentSection;
	}
}
