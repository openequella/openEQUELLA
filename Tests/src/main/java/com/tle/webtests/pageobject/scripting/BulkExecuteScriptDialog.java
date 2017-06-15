package com.tle.webtests.pageobject.scripting;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.searching.BulkActionDialog;
import com.tle.webtests.pageobject.searching.BulkPreviewPage;
import com.tle.webtests.pageobject.searching.BulkResultsPage;

public class BulkExecuteScriptDialog extends AbstractPage<BulkExecuteScriptDialog>
{
	@FindBy(id = "beso_vs")
	private WebElement checkSyntaxButton;
	@FindBy(id = "errordisplay")
	private WebElement errorAjaxDiv;
	@FindBy(id = "validationstatus")
	private WebElement statusDiv;

	private BulkActionDialog dialog;

	public BulkExecuteScriptDialog(BulkActionDialog dialog)
	{
		super(dialog.getContext(), By.xpath("//h3[text()='Execute script']"));
		this.dialog = dialog;
	}

	public void typeCode(String text)
	{
		((JavascriptExecutor) driver).executeScript("cmbeso_scpt.setValue('" + text + "');");
	}

	public void checkSyntax()
	{
		WaitingPageObject<BulkExecuteScriptDialog> ajaxUpdateExpext = ajaxUpdateExpect(errorAjaxDiv, statusDiv);
		checkSyntaxButton.click();
		ajaxUpdateExpext.get();

	}

	public boolean syntaxError()
	{
		return statusDiv.getAttribute("class").equals("fail");
	}

	public boolean syntaxPass()
	{
		return statusDiv.getAttribute("class").equals("ok");
	}

	public BulkResultsPage execute()
	{
		return dialog.preview().execute();
	}

	public BulkPreviewPage preview()
	{
		return dialog.preview();
	}

	public boolean errorMessageContains(String text)
	{
		if( !syntaxError() )
		{
			return false;
		}
		String errorMessage = driver.findElement(By.xpath("//div[@id='errormessage']/pre")).getText();
		return errorMessage.contains(text);
	}

}
