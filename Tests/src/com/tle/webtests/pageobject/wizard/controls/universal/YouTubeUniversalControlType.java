package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class YouTubeUniversalControlType extends AbstractUniversalControlType<YouTubeUniversalControlType>
{
	@FindBy(id = "{wizid}_dialog_yh_query")
	private WebElement searchField;
	@FindBy(id = "{wizid}_dialog_yh_search")
	private WebElement searchButton;
	@FindBy(xpath = "id('{wizid}_dialog')//div[contains(@class,'youTubeHandler')]")
	private WebElement mainDiv;
	@FindBy(id = "{wizid}_dialog_yh_displayName")
	protected WebElement nameField;

	@FindBy(id = "{wizid}_dialog_yh_channelList")
	private WebElement channelList;

	public YouTubeUniversalControlType(UniversalControl control)
	{
		super(control);
	}

	public YouTubeUniversalControlType search(String searchTerm, String channelName)
	{
		searchField.clear();
		searchField.sendKeys(searchTerm);

		if( channelName != null )
		{
			WaitingPageObject<YouTubeUniversalControlType> submitWaiter = submitWaiter();
			EquellaSelect select = new EquellaSelect(context, channelList);
			select.selectByVisibleText(channelName);
			submitWaiter.get();
		}

		WaitingPageObject<YouTubeUniversalControlType> submitWaiter = submitWaiter();
		searchButton.click();
		return submitWaiter.get();
	}

	public UniversalControl addVideos(int... indexes)
	{
		return selectVideos(control.attachmentCountExpectation(indexes.length), indexes);
	}

	private <T extends PageObject> T selectVideos(WaitingPageObject<T> returnTo, int... indexes)
	{
		for( int i = 0; i < indexes.length; i++ )
		{
			WaitingPageObject<YouTubeUniversalControlType> submitWaiter = submitWaiter();
			driver.findElement(By.id(page.subComponentId(ctrlnum, "dialog_yh_results_" + (indexes[i] - 1)))).click();
			submitWaiter.get();
		}
		WaitingPageObject<T> disappearWaiter = ExpectWaiter.waiter(removalCondition(addButton), returnTo);
		addButton.click();
		return disappearWaiter.get();
	}

	public GenericAttachmentEditPage selectVideo(int index)
	{
		return selectVideos(editPage(), index);
	}

	@Override
	public String getType()
	{
		return "YouTube";
	}

	@Override
	public WebElement getFindElement()
	{
		return mainDiv;
	}

	@Override
	public WebElement getNameField()
	{
		return nameField;
	}
}
