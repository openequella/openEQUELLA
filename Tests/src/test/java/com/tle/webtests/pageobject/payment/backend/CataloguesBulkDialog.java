package com.tle.webtests.pageobject.payment.backend;

import org.openqa.selenium.By;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.generic.component.ShuffleBox;
import com.tle.webtests.pageobject.searching.BulkActionDialog;
import com.tle.webtests.pageobject.searching.BulkResultsPage;

public class CataloguesBulkDialog extends AbstractPage<CataloguesBulkDialog>
{

	private BulkActionDialog dialog;
	private ShuffleBox catalogueShuffle;


	public CataloguesBulkDialog(BulkActionDialog dialog)
	{
		super(dialog.getContext(), By.xpath("//h3[text()='Select one or more catalogues']"));
		this.dialog = dialog;
		waitForElement(By.id("bcco_cl"));
		catalogueShuffle = new ShuffleBox(dialog.getContext(), "bcco_cl");
		catalogueShuffle.checkLoaded();
	}

	public ShuffleBox getCatalogueShuffleBox()
	{
		return catalogueShuffle;
	}

	public BulkResultsPage executeBulk()
	{
		dialog.execute();
		return new BulkResultsPage(context).get();
	}

}
