package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class GoogleBooksUniversalControlType extends AbstractUniversalControlType<GoogleBooksUniversalControlType>
{

	@FindBy(id = "{wizid}_dialog_gbh_query")
	private WebElement searchField;
	@FindBy(id = "{wizid}_dialog_gbh_search")
	private WebElement searchButton;
	@FindBy(xpath = "id('{wizid}_dialog')//div[contains(@class,'googleBookHandler')]")
	private WebElement mainDiv;
	@FindBy(id = "{wizid}_dialog_gbh_displayName")
	protected WebElement nameField;

	public GoogleBooksUniversalControlType(UniversalControl control)
	{
		super(control);
	}

	@Override
	public WebElement getFindElement()
	{
		return mainDiv;
	}

	@Override
	public String getType()
	{
		return "Google Books";
	}

	public GoogleBooksUniversalControlType search(String searchTerm)
	{
		searchField.clear();
		searchField.sendKeys(searchTerm);
		WaitingPageObject<GoogleBooksUniversalControlType> submitWaiter = submitWaiter();
		searchButton.click();
		return submitWaiter.get();
	}

	public GenericAttachmentEditPage selectBook(int index)
	{
		return selectBooks(editPage(), index).get();
	}

	private <T extends PageObject> T selectBooks(WaitingPageObject<T> returnTo, int... indexes)
	{
		for( int i = 0; i < indexes.length; i++ )
		{
			WaitingPageObject<GoogleBooksUniversalControlType> submitWaiter = submitWaiter();
			driver.findElement(By.id(page.subComponentId(ctrlnum, "dialog_gbh_results_" + (indexes[i] - 1)))).click();
			submitWaiter.get();
		}

		WaitingPageObject<T> disappearWaiter = ExpectWaiter.waiter(removalCondition(addButton), returnTo);
		addButton.click();
		return disappearWaiter.get();
	}

	public UniversalControl addBooks(int... indexes)
	{
		return selectBooks(control.attachmentCountExpectation(indexes.length), indexes);
	}

	@Override
	public WebElement getNameField()
	{
		return nameField;
	}
}
