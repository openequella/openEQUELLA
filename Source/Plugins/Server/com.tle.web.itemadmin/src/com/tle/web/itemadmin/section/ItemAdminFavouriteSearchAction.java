package com.tle.web.itemadmin.section;

import javax.inject.Inject;

import com.tle.web.search.actions.AbstractFavouriteSearchAction;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.standard.annotations.Component;

public class ItemAdminFavouriteSearchAction extends AbstractFavouriteSearchAction
{
	@Inject
	@Component(name = "fd")
	private ItemAdminFavouriteSearchDialog favDialog;

	@Override
	protected EquellaDialog<?> getDialog()
	{
		return favDialog;
	}
}
