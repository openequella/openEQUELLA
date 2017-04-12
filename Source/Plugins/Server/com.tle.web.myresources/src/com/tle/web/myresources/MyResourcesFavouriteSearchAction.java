package com.tle.web.myresources;

import javax.inject.Inject;

import com.tle.web.search.actions.AbstractFavouriteSearchAction;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.standard.annotations.Component;

public class MyResourcesFavouriteSearchAction extends AbstractFavouriteSearchAction
{
	@Inject
	@Component(name = "fd")
	private MyResourcesFavouriteSearchDialog dialog;

	@Override
	protected EquellaDialog<?> getDialog()
	{
		return dialog;
	}
}
