package com.tle.webtests.pageobject.payment.backend;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.generic.entities.AbstractShowEntitiesPage;

/**
 * @author Aaron
 */
public class ShowTaxesPage extends AbstractShowEntitiesPage<ShowTaxesPage>
{
	private final static String ALERT_DELETE_FAIL = "This tax is in use and cannot be deleted";

	public ShowTaxesPage(PageContext context)
	{
		super(context);
	}

	@Override
	public String getSectionId()
	{
		return "st";
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/tax.do");
	}

	@Override
	protected String getH2Title()
	{
		return "Store taxes";
	}

	@Override
	protected String getEmptyText()
	{
		return "There are no editable taxes";
	}

	public void deleteTaxFail(PrefixedName name)
	{
		deleteEntityFail(name, ALERT_DELETE_FAIL);
	}

	public EditTaxPage createTax()
	{
		return createEntity(new EditTaxPage(this));
	}

	public EditTaxPage editTax(PrefixedName tax)
	{
		return editEntity(new EditTaxPage(this), tax);
	}

	public EditTaxPage cloneTax(PrefixedName tax)
	{
		return cloneEntity(new EditTaxPage(this), tax);
	}

	@Override
	public void deleteAllNamed(PrefixedName... names)
	{
		for( PrefixedName name : names )
		{
			while( entityExists(name) )
			{
				deleteEntity(name);
			}
		}
	}
}
