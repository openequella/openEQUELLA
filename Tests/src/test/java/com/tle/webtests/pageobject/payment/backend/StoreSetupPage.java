package com.tle.webtests.pageobject.payment.backend;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;

public class StoreSetupPage extends AbstractPage<StoreSetupPage>
{
	private static final String TITLE = "Store setup";

	@FindBy(id = "_allow")
	private WebElement allowCheckBox;
	@FindBy(id = "_name")
	private WebElement nameField;
	@FindBy(id = "_description")
	private WebElement descriptionField;

	@FindBy(id = "_smallFile")
	private WebElement iconUploadInput;
	@FindBy(id = "_largeFile")
	private WebElement imageUploadInput;
	@FindBy(id = "_smallUploadButton")
	private WebElement iconUploadButton;
	@FindBy(id = "_largeUploadButton")
	private WebElement imageUploadButton;
	@FindBy(id = "_smallDeleteIconButton")
	private WebElement removeIconButton;
	@FindBy(id = "_largeDeleteIconButton")
	private WebElement removeImageButton;

	@FindBy(id = "_saveButton")
	private WebElement saveButton;

	@FindBy(id = "_contact_name")
	private WebElement contactNameField;
	@FindBy(id = "_contact_number")
	private WebElement contactNumberField;
	@FindBy(id = "_contact_email")
	private WebElement contactEmailField;

	@FindBy(id = "overallajaxdiv")
	private WebElement ajaxDiv;

	public enum Fields
	{
		NAME, DESCRIPTION, CONTACT_NAME, CONTACT_NUMBER, CONTACT_EMAIL
	}

	public StoreSetupPage(PageContext context)
	{
		super(context, By.xpath("//div[@class='area']/h2[text()= " + quoteXPath(TITLE) + "]"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/storesettings.do");
	}

	public void setDetails(String name, String description, String contactName, String contactNumber,
		String contactEmail)
	{
		nameField.clear();
		nameField.sendKeys(name);
		descriptionField.clear();
		descriptionField.sendKeys(description);
		contactNameField.clear();
		contactNameField.sendKeys(contactName);
		contactNumberField.clear();
		contactNumberField.sendKeys(contactNumber);
		contactEmailField.clear();
		contactEmailField.sendKeys(contactEmail);
	}

	public List<String> getDetails()
	{
		List<String> list = new ArrayList<String>();
		list.add(nameField.getAttribute("value"));
		list.add(descriptionField.getText());
		list.add(contactNameField.getAttribute("value"));
		list.add(contactNumberField.getAttribute("value"));
		list.add(contactEmailField.getAttribute("value"));
		return list;
	}

	public void setEnabled(boolean allow)
	{
		if( allowCheckBox.isSelected() != allow )
		{
			WaitingPageObject<StoreSetupPage> waiter = ajaxUpdateExpect(ajaxDiv, saveButton);
			allowCheckBox.click();
			waiter.get();
		}
	}

	public StoreSetupPage uploadIcon(URL icon)
	{
		waitForHiddenElement(iconUploadInput);
		WaitingPageObject<StoreSetupPage> visibilityWaiter = visibilityWaiter(removeIconButton);
		iconUploadInput.sendKeys(getPathFromUrl(icon));
		iconUploadButton.click();
		return visibilityWaiter.get();
	}

	public StoreSetupPage uploadImage(URL image)
	{
		waitForHiddenElement(imageUploadInput);
		WaitingPageObject<StoreSetupPage> visibilityWaiter = visibilityWaiter(removeImageButton);
		imageUploadInput.sendKeys(getPathFromUrl(image));
		imageUploadButton.click();
		return visibilityWaiter.get();
	}

	public StoreSetupPage save()
	{
		clickAndUpdate(saveButton).get();
		return ReceiptPage.waiter("Settings saved successfully", this).get();
	}

	public StoreSetupPage saveWithFail()
	{
		return clickAndUpdate(saveButton).get();
	}

	public boolean isFormHidden()
	{
		return !isPresent(nameField);
	}

	public boolean hasIcon()
	{
		return isPresent(removeIconButton);
	}

	public boolean hasImage()
	{
		return isPresent(removeImageButton);
	}

	public void deleteIcon()
	{
		WaitingPageObject<StoreSetupPage> ajaxUpdate = ajaxUpdate(ajaxDiv);
		removeIconButton.click();
		ajaxUpdate.get();
	}

	public void deleteImage()
	{
		WaitingPageObject<StoreSetupPage> ajaxUpdate = ajaxUpdate(ajaxDiv);
		removeImageButton.click();
		ajaxUpdate.get();
	}

	public String getContactName()
	{
		return contactNameField.getAttribute("value");
	}

	public String getContactNumber()
	{
		return contactNumberField.getAttribute("value");
	}

	public String getContactEmail()
	{
		return contactEmailField.getAttribute("value");
	}

	public boolean isFieldInvalid(Fields field)
	{
		WebElement base = null;

		switch( field )
		{
			case NAME:
				base = contactNameField;
				break;
			case DESCRIPTION:
				base = descriptionField;
				break;
			case CONTACT_NAME:
				base = contactNameField;
				break;
			case CONTACT_NUMBER:
				base = contactNumberField;
				break;
			case CONTACT_EMAIL:
				base = contactEmailField;
				break;
		}

		return isPresent(base.findElement(By.xpath("./../p[contains(text(), 'You must ')]")));
	}
}
