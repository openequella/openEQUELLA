package com.tle.web.search.actions;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;

@NonNullByDefault
@Bind
public class StandardFavouriteSearchDialog extends AbstractFavouriteSearchDialog
{
	@Inject
	private StandardFavouriteSearchSection contentSection;

	@Override
	protected AbstractFavouriteSearchSection getContentSection()
	{
		return contentSection;
	}
}
