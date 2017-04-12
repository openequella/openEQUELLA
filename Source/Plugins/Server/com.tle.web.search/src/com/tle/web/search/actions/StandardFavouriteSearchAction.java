package com.tle.web.search.actions;

import javax.inject.Inject;

import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author Aaron
 */
public class StandardFavouriteSearchAction extends AbstractFavouriteSearchAction
{
	@Inject
	@Component(name = "fd")
	private StandardFavouriteSearchDialog favDialog;

	@Override
	protected EquellaDialog<?> getDialog()
	{
		return favDialog;
	}
}
