package com.tle.web.connectors.manage;

import javax.inject.Inject;

import com.tle.web.search.actions.AbstractFavouriteSearchAction;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.standard.annotations.Component;

public class ConnectorManagementFavouriteSearchAction extends AbstractFavouriteSearchAction
{
	@Inject
	@Component(name = "fd")
	private ConnectorManagementFavouriteSearchDialog dialog;

	@Override
	protected EquellaDialog<?> getDialog()
	{
		return dialog;
	}
}
