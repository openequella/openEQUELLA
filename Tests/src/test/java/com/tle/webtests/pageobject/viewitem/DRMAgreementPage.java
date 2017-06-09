package com.tle.webtests.pageobject.viewitem;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;

public class DRMAgreementPage extends AbstractPage<DRMAgreementPage>
{
	@FindBy(id = "drm_acceptButton")
	private WebElement acceptButton;
	@FindBy(id = "drm_rejectButton")
	private WebElement rejectButton;
	@FindBy(id = "drm_previewButton")
	private WebElement previewButton;

	public DRMAgreementPage(PageContext context)
	{
		super(context, By.id("drm_rejectButton"));
	}

	public <T extends PageObject> T preview(WaitingPageObject<T> targetPage)
	{
		previewButton.click();
		return targetPage.get();
	}

	public <T extends PageObject> T reject(WaitingPageObject<T> targetPage)
	{
		rejectButton.click();
		return targetPage.get();
	}

	public <T extends PageObject> T acceptThisIfYouAreVerySureYouNeedToOtherwiseUsePreview(
		WaitingPageObject<T> targetPage)
	{
		acceptButton.click();
		return targetPage.get();
	}

	public boolean hasTerms(String terms)
	{
		try
		{
			WebElement termsDiv = driver.findElement(By.className("terms"));
			return termsDiv.getText().equals(terms);
		}
		catch( NoSuchElementException e )
		{
			return false;
		}
	}

}
