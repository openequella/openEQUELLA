package com.tle.web.connectors.manage;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.search.actions.AbstractFavouriteSearchDialog;
import com.tle.web.search.actions.AbstractFavouriteSearchSection;

@Bind
public class ConnectorManagementFavouriteSearchDialog extends AbstractFavouriteSearchDialog
{
	@Inject
	private ConnectorManagementFavouriteSearchSection contentSection;

	@Override
	protected AbstractFavouriteSearchSection getContentSection()
	{
		return contentSection;
	}
}
