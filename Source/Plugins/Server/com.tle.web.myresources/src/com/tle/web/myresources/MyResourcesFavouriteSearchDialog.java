package com.tle.web.myresources;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.search.actions.AbstractFavouriteSearchDialog;
import com.tle.web.search.actions.AbstractFavouriteSearchSection;

@Bind
public class MyResourcesFavouriteSearchDialog extends AbstractFavouriteSearchDialog
{
	@Inject
	private MyResourcesFavouriteSearchSection contentSection;

	@Override
	protected AbstractFavouriteSearchSection getContentSection()
	{
		return contentSection;
	}
}
