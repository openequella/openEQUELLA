package com.tle.webtests.pageobject.payment.backend;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import com.tle.webtests.pageobject.generic.entities.AbstractEditEntityPage;

public class EditStoreFrontRegistrationPage
	extends
		AbstractEditEntityPage<EditStoreFrontRegistrationPage, ShowStoreFrontRegistrationsPage>
{
	public static final String VALIDATION_MISSING_APPLICATION = "Application is mandatory";
	public static final String VALIDATION_MISSING_VERSION = "Version is mandatory";
	public static final String VALIDATION_MISSING_COUNTRY = "Country is mandatory";
	public static final String VALIDATION_MISSING_CLIENT_ID = "OAuth client ID is mandatory";
	public static final String VALIDATION_DUPLICATE_CLIENT_ID = "OAuth client ID is already in use";
	public static final String VALIDATION_MISSING_REDIRECT_URL = "OAuth redirect URL is mandatory";
	public static final String VALIDATION_MISSING_USER = "User is mandatory";
	// Not actually possible using the UI
	public static final String VALIDATION_INVALID_COUNTRY = "Please select a valid country";

	@FindBy(id = "{editorSectionId}_af")
	protected WebElement freeCheckbox;
	@FindBy(id = "{editorSectionId}_ap")
	protected WebElement purchaseCheckbox;
	@FindBy(id = "{editorSectionId}_as")
	protected WebElement subCheckbox;

	@FindBy(id = "{editorSectionId}_pn")
	protected WebElement applicationField;
	@FindBy(id = "{editorSectionId}_pv")
	protected WebElement versionField;
	@FindBy(id = "{editorSectionId}_cl")
	protected WebElement countryDropdown;

	@FindBy(id = "{editorSectionId}_ci")
	protected WebElement clientIDField;
	@FindBy(id = "{editorSectionId}_ru")
	protected WebElement redirectURLField;
	@FindBy(id = "{editorSectionId}_sub")
	protected WebElement storeFrontUserButton;

	@FindBy(id = "{editorSectionId}_e")
	protected WebElement enabledCheckbox;

	@FindBy(id = "userAjaxDiv")
	protected WebElement userAjaxDiv;

	@FindBy(id = "{editorSectionId}_tt")
	protected WebElement taxDropdown;

	public EditStoreFrontRegistrationPage(ShowStoreFrontRegistrationsPage listPage)
	{
		super(listPage);
	}

	@Override
	protected String getEntityName()
	{
		return "store front registration";
	}

	@Override
	protected String getContributeSectionId()
	{
		return "sfc";
	}

	@Override
	protected String getEditorSectionId()
	{
		return "sfe";
	}

	@Override
	public EditStoreFrontRegistrationPage setName(String name)
	{
		return super.setName(name);
	}

	public EditStoreFrontRegistrationPage setTax(PrefixedName taxName)
	{
		EquellaSelect select = new EquellaSelect(context, taxDropdown);
		if( taxName == null )
		{
			select.selectByVisibleText("No tax");
		}
		else
		{
			select.selectByVisibleText(taxName.toString());
		}
		return this;
	}

	public String getSelectedTax()
	{
		EquellaSelect select = new EquellaSelect(context, taxDropdown);
		return select.getSelectedText();
	}

	public EditStoreFrontRegistrationPage setPricingModels(boolean free, boolean purchase, boolean subscription)
	{
		if( free && !freeCheckbox.isSelected() )
		{
			freeCheckbox.click();
		}
		if( purchase && !purchaseCheckbox.isSelected() )
		{
			purchaseCheckbox.click();
		}
		if( subscription && !subCheckbox.isSelected() )
		{
			subCheckbox.click();
		}
		return this;
	}

	public EditStoreFrontRegistrationPage setApplication(String application)
	{
		applicationField.clear();
		applicationField.sendKeys(application);
		return this;
	}

	public EditStoreFrontRegistrationPage setVersion(String version)
	{
		versionField.clear();
		versionField.sendKeys(version);
		return this;
	}

	/**
	 * Uses country code e.g. AU, GB, US
	 */
	public EditStoreFrontRegistrationPage setCountry(String countryCode)
	{
		new EquellaSelect(context, countryDropdown).selectByValue(countryCode);
		return this;
	}

	public EditStoreFrontRegistrationPage setClientId(String clientId)
	{
		clientIDField.clear();
		clientIDField.sendKeys(clientId);
		return this;
	}

	public EditStoreFrontRegistrationPage setRedirectUrl(String redirectURL)
	{
		redirectURLField.clear();
		redirectURLField.sendKeys(redirectURL);
		return this;
	}

	public EditStoreFrontRegistrationPage setStoreFrontUser(String username)
	{
		storeFrontUserButton.click();
		SelectUserDialog dialog = new SelectUserDialog(context, "sfe_sud").get();
		return dialog.search(username).selectAndFinish(username, ajaxUpdate(userAjaxDiv));
	}

	public EditStoreFrontRegistrationPage setEnabled(boolean enabled)
	{
		if( enabled && !enabledCheckbox.isSelected() )
		{
			enabledCheckbox.click();
		}
		if( !enabled && enabledCheckbox.isSelected() )
		{
			enabledCheckbox.click();
		}
		return this;
	}

	public boolean isTransactionsInvalid()
	{
		try
		{
			freeCheckbox.findElement(By.xpath("./../../p[@class='ctrlinvalidmessage']"));
			return true;
		}
		catch( NoSuchElementException e )
		{
			return false;
		}
	}

	public String getApplicationValidationMessage()
	{
		return invalidMessage(applicationField);
	}

	public String getClientIdValidationMessage()
	{
		return invalidMessage(clientIDField);
	}

	public String getRedirectUrlValidationMessage()
	{
		return invalidMessage(redirectURLField);
	}

	public String getVersionValidationMessage()
	{
		return invalidMessage(versionField);
	}

	public String getCountryValidationMessage()
	{
		return invalidMessage(countryDropdown);
	}

	public String getUserValidationMessage()
	{
		return invalidMessage(storeFrontUserButton);
	}

	public boolean isFreeChecked()
	{
		return freeCheckbox.isSelected();
	}

	public boolean isOutrightChecked()
	{
		return purchaseCheckbox.isSelected();
	}

	public boolean isSubscriptionChecked()
	{
		return subCheckbox.isSelected();
	}
}
