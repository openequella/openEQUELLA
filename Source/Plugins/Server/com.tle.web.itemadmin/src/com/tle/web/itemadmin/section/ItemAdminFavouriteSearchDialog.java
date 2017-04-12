package com.tle.web.itemadmin.section;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.search.actions.AbstractFavouriteSearchDialog;
import com.tle.web.search.actions.AbstractFavouriteSearchSection;

/**
 * @author Aaron
 */
@Bind
public class ItemAdminFavouriteSearchDialog extends AbstractFavouriteSearchDialog
{
	@Inject
	private ItemAdminFavouriteSearchSection contentSection;

	@Override
	protected AbstractFavouriteSearchSection getContentSection()
	{
		return contentSection;
	}
}
