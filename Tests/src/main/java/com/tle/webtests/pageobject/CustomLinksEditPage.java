package com.tle.webtests.pageobject;

import java.net.URL;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.webtests.pageobject.generic.component.MultiLingualEditbox;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CustomLinksEditPage extends AbstractPage<CustomLinksEditPage>
{
	@FindBy(id = "cls_saveButton")
	private WebElement saveButton;
	private MultiLingualEditbox titleField;

	@FindBy(id = "cls_uf")
	private WebElement urlField;

	@FindBy(id = "cls_downloadButton")
	private WebElement downloadButton;

	@FindBy(id = "cls_cancelButton")
	private WebElement cancelButton;

	@FindBy(id = "cls_uploadButton")
	private WebElement uploadButton;

	@FindBy(id = "cls_file")
	private WebElement fileUpload;

	@FindBy(id = "cls_deleteIconButton")
	private WebElement deleteIcon;
	private CustomLinksPage customLinksPage;

	public CustomLinksEditPage(CustomLinksPage customLinksPage)
	{
		super(customLinksPage.getContext()); // url edit box
		this.customLinksPage = customLinksPage;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return urlField;
	}

	@Override
	protected void checkLoadedElement()
	{
		super.checkLoadedElement();
		titleField = new MultiLingualEditbox(context, "cls_dn").get();
	}

	public CustomLinksPage save(String displayName)
	{
		return save(customLinksPage.getNewExpectation(displayName, urlField.getAttribute("value")));
	}

	public CustomLinksPage save()
	{
		return save(customLinksPage.getNewExpectation(titleField.getCurrentString(), urlField.getAttribute("value")));
	}

	private CustomLinksPage save(WaitingPageObject<CustomLinksPage> returnTo)
	{
		saveButton.click();
		return returnTo.get();
	}

	public void setName(String name)
	{
		titleField.setCurrentString(name);
	}

	public void setUrl(String url)
	{
		urlField.clear();
		urlField.sendKeys(url);
	}

	private By getCurrentIconBy()
	{
		return By.xpath("id('currentIcon')/div");
	}


	public WebElement getCurrentIcon()
	{
		return find(driver, getCurrentIconBy());
	}


	public void downloadIcon()
	{
		downloadButton.click();
		waiter.until(ExpectedConditions.visibilityOfElementLocated(getCurrentIconBy()));
	}

	public void cancel()
	{
		cancelButton.click();
	}

	public void uploadIcon(URL icon)
	{
		ExpectedCondition<?> iconUpdate = ExpectedConditions2.ajaxUpdate(getCurrentIcon());
		waitForHiddenElement(fileUpload);
		fileUpload.sendKeys(getPathFromUrl(icon));
		waiter.until(iconUpdate);
	}

	public void deleteIcon()
	{
		ExpectedCondition<Boolean> deleted = ExpectedConditions2.stalenessOrNonPresenceOf(deleteIcon);
		deleteIcon.click();
		waiter.until(deleted);
	}

	public MultiLingualEditbox getTitleSection()
	{
		return titleField;
	}

}
