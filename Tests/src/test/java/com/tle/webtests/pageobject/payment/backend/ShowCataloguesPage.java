package com.tle.webtests.pageobject.payment.backend;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.generic.entities.AbstractShowEntitiesPage;

/**
 * @author Dinuk
 */
public class ShowCataloguesPage extends AbstractShowEntitiesPage<ShowCataloguesPage>
{
	public static String DELETE_CONFIRMATION = "Are you sure you want to delete this catalogue?";
	public static String EMPTY_TEXT = "There are no editable catalogues";

	public ShowCataloguesPage(PageContext context)
	{
		super(context);
	}

	public EditCataloguePage createCatalogue()
	{
		return createEntity(new EditCataloguePage(this));
	}

	public EditCataloguePage editCatalogue(PrefixedName name)
	{
		return editEntity(new EditCataloguePage(this), name);
	}

	public ShowCataloguesPage deleteCatalogue(PrefixedName catalogueName)
	{
		return deleteEntity(catalogueName, DELETE_CONFIRMATION);
	}

	@Override
	protected String getSectionId()
	{
		return "sc";
	}

	@Override
	protected String getH2Title()
	{
		return "Catalogues";
	}

	@Override
	protected String getEmptyText()
	{
		return EMPTY_TEXT;
	}
}
