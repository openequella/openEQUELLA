package com.tle.webtests.pageobject.payment.storefront;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.pageobject.oauth.OAuthLogonPage;

public class StoreRegistrationEditPage extends AbstractPage<StoreRegistrationEditPage>
{
	private static final String CREATE_TITLE = "Create store registration";
	private static final String EDIT_TITLE = "Edit store registration";

	@FindBy(id = "sr_storeUrl")
	private WebElement storeURLField;
	@FindBy(id = "sr_clientId")
	private WebElement clientIdField;
	@FindBy(id = "sr_connectButton")
	private WebElement connectButton;
	@FindBy(id = "sr_e")
	private WebElement enabledCheckbox;
	@FindBy(id = "sr_sv")
	private WebElement saveButton;
	@FindBy(id = "sr_cl")
	private WebElement cancelButton;

	public StoreRegistrationEditPage(PageContext context, boolean create)
	{
		super(context);
		if( create )
		{
			loadedBy = By.xpath("//div[@class='area']/h2[text()= " + quoteXPath(CREATE_TITLE) + "]");
		}
		else
		{
			loadedBy = By.xpath("//div[@class='area']/h2[text()= " + quoteXPath(EDIT_TITLE) + "]");
		}
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/registerstore.do");
	}

	public StoreRegistrationEditPage setStoreUrl(String storeUrl)
	{
		storeURLField.clear();
		storeURLField.sendKeys(storeUrl);
		return this;
	}

	public StoreRegistrationEditPage setClientId(String clientId)
	{
		clientIdField.clear();
		clientIdField.sendKeys(clientId);
		return this;
	}

	public OAuthLogonPage connectToStore()
	{
		connectButton.click();
		return new OAuthLogonPage(context).get();
	}

	public StoreRegistrationEditPage connectWithErrors()
	{
		connectButton.click();
		return visibilityWaiter(driver, By.className("ctrlinvalidmessage")).get();
	}

	public ErrorPage connectToErrorPage()
	{
		connectButton.click();
		return new ErrorPage(context).get();
	}

	public void waitForCheckbox()
	{
		waitForElement(enabledCheckbox);
	}

	public StoreRegistrationEditPage enableStore()
	{
		if( !enabledCheckbox.isSelected() )
		{
			enabledCheckbox.click();
		}
		return this;
	}

	public StoreRegistrationPage saveStoreRegistration()
	{
		saveButton.click();
		return new StoreRegistrationPage(context).get();
	}

	public StoreRegistrationEditPage saveWithErrors()
	{
		saveButton.click();
		return this;
	}

	public boolean isSaveButtonHidden()
	{
		return !isPresent(saveButton);
	}

	public StoreRegistrationPage cancelStoreRegistration()
	{
		cancelButton.click();
		return new StoreRegistrationPage(context).get();
	}

	public boolean isClientIdInvalid()
	{
		if( isPresent(clientIdField.findElement(By.xpath(".//following-sibling::p"))) )
		{
			return true;
		}
		return true;
	}

	public boolean isURLInvalid()
	{
		if( isPresent(storeURLField.findElement(By.xpath(".//following-sibling::p"))) )
		{
			return true;
		}
		return true;
	}

	public String getInvalidURLMessage()
	{
		if( isURLInvalid() )
		{
			return storeURLField.findElement(By.xpath(".//following-sibling::p")).getText();
		}
		return "";
	}

	public String getRedirectUrl()
	{
		String temp = storeURLField.getAttribute("value");
		return storeURLField.getAttribute("value");
	}
}
