package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.payment.backend.CataloguesBulkDialog;
import com.tle.webtests.pageobject.payment.backend.PricingTiersBulkDialog;
import com.tle.webtests.pageobject.scripting.BulkExecuteScriptDialog;
import com.tle.webtests.pageobject.viewitem.MoveCloneDialog;

public class BulkSection extends AbstractPage<BulkSection>
{
	@FindBy(id = "bss_selectAllButton")
	private WebElement allLink;
	@FindBy(id = "bss_executeButton")
	private WebElement executeButton;

	private AbstractBulkResultsPage<?, ?, ?> results;

	public BulkSection(AbstractBulkResultsPage<?, ?, ?> results)
	{
		super(results.getContext());
		this.results = results;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return executeButton;
	}

	public boolean deleteAll()
	{
		return commandAll("delete");
	}

	public BulkSection selectAll()
	{
		WaitingPageObject<?> waiter = results.getResultsUpdateWaiter();
		allLink.click();
		waiter.get();
		return get();
	}

	public boolean purgeAll()
	{
		return commandAll("purge");
	}

	public boolean commandAll(String command)
	{
		selectAll();
		return executeCommand(command);
	}

	private BulkActionDialog performAction()
	{
		executeButton.click();
		return new BulkActionDialog(context).get();
	}

	public boolean executeCommand(String command)
	{
		BulkActionDialog dialog = performAction();
		dialog.selectOp(command);
		BulkResultsPage resultsPage = dialog.execute().waitForAll();
		boolean noErrors = resultsPage.noErrors();
		resultsPage.close(results.getResultsUpdateWaiter());
		return noErrors;
	}

	public BulkActionDialog executeCommandPage(String command)
	{
		BulkActionDialog dialog = performAction();
		dialog.selectOp(command);
		dialog.next();
		return dialog;
	}

	public BulkResultsPage executeCommandFailure(String command)
	{
		BulkActionDialog dialog = performAction();
		dialog.selectOp(command);
		return dialog.execute();
	}

	@Override
	public MoveCloneDialog clone()
	{
		BulkActionDialog dialog = executeCommandPage("clone");
		return new MoveCloneDialog(dialog).get();
	}

	public MoveCloneDialog move()
	{
		BulkActionDialog dialog = executeCommandPage("move");
		return new MoveCloneDialog(dialog).get();
	}

	public CataloguesBulkDialog addCatalogues()
	{
		BulkActionDialog dialog = executeCommandPage("addcatalogues");
		return new CataloguesBulkDialog(dialog).get();
	}

	public CataloguesBulkDialog removeCatalogues()
	{
		BulkActionDialog dialog = executeCommandPage("removecatalogues");
		return new CataloguesBulkDialog(dialog).get();
	}

	public PricingTiersBulkDialog tiers()
	{
		BulkActionDialog dialog = executeCommandPage("changetier");
		return new PricingTiersBulkDialog(dialog).get();
	}

	public BulkExecuteScriptDialog exectueScript()
	{
		BulkActionDialog dialog = executeCommandPage("executescript");
		return new BulkExecuteScriptDialog(dialog).get();
	}

	public EditMetadataDialog editMetadata()
	{
		BulkActionDialog dialog = executeCommandPage("editmetadata");
		return new EditMetadataDialog(dialog).get();
	}

}
