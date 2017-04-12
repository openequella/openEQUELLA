package com.tle.web.hierarchy.section;

import javax.inject.Inject;

import com.tle.web.search.actions.AbstractFavouriteSearchAction;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.standard.annotations.Component;

public class HierarchyFavouriteSearchAction extends AbstractFavouriteSearchAction
{
	@Inject
	@Component(name = "fd")
	private HierarchyFavouriteSearchDialog dialog;

	@Override
	protected EquellaDialog<?> getDialog()
	{
		return dialog;
	}
}
