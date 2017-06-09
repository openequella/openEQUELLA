package com.tle.webtests.pageobject.payment.backend;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

/**
 * TODO: would be nice to use AbstractEditEntityPage
 * 
 * @author Aaron
 */
public class EditTierPage extends AbstractPage<EditTierPage>
{
	@FindBy(xpath = "id('{editorSectionId}_t')/div/input")
	private WebElement nameField;
	@FindBy(xpath = "id('{editorSectionId}_d')/div/textarea")
	private WebElement descriptionField;

	@FindBy(xpath = "id('{editorSectionId}_e0')")
	private WebElement enablePeriod0;
	@FindBy(xpath = "id('{editorSectionId}_e1')")
	private WebElement enablePeriod1;
	@FindBy(xpath = "id('{editorSectionId}_e2')")
	private WebElement enablePeriod2;
	@FindBy(xpath = "id('{editorSectionId}_e3')")
	private WebElement enablePeriod3;
	@FindBy(xpath = "id('{editorSectionId}_e4')")
	private WebElement enablePeriod4;

	@FindBy(xpath = "id('{editorSectionId}_p0')")
	private WebElement period0;
	@FindBy(xpath = "id('{editorSectionId}_p1')")
	private WebElement period1;
	@FindBy(xpath = "id('{editorSectionId}_p2')")
	private WebElement period2;
	@FindBy(xpath = "id('{editorSectionId}_p3')")
	private WebElement period3;
	@FindBy(xpath = "id('{editorSectionId}_p4')")
	private WebElement period4;

	@FindBy(id = "{contributeSectionId}_sv")
	protected WebElement saveButton;
	@FindBy(id = "{contributeSectionId}_cl")
	protected WebElement cancelButton;

	private final boolean creating;
	private final boolean subscription;

	protected EditTierPage(PageContext context, boolean creating, boolean subscription)
	{
		super(context);
		this.creating = creating;
		this.subscription = subscription;
		loadedBy = getLoadedBy();
	}

	protected By getLoadedBy()
	{
		return By.xpath("//h2[text()="
			+ quoteXPath((creating ? "Create " : "Edit ") + (subscription ? "subscription" : "purchase")
				+ " pricing tier") + "]");
	}

	public String getEditorSectionId()
	{
		return "pte";
	}

	public String getContributeSectionId()
	{
		return "tc";
	}

	public EditTierPage setName(String name)
	{
		nameField.clear();
		nameField.sendKeys(name);
		return this;
	}

	public String getName()
	{
		return nameField.getAttribute("value");
	}

	public EditTierPage setDescription(String description)
	{
		descriptionField.clear();
		descriptionField.sendKeys(description);
		return this;
	}

	public String getDescription()
	{
		return descriptionField.getAttribute("value");
	}

	public EditTierPage enablePeriod(int index, boolean enable)
	{
		final WebElement cb = getPeriodCheckbox(index);
		final boolean checked = cb.isSelected();
		if( checked && !enable || !checked && enable )
		{
			cb.click();
		}

		final WebElement pv = getPeriodPrice(index);
		if( enable )
		{
			waitForElement(pv);
		}
		else
		{
			waitForElementInvisibility(pv);
		}
		return this;
	}

	/**
	 * Index will be zero for purchases
	 * 
	 * @param price
	 * @param index
	 * @return
	 */
	public EditTierPage setPrice(int index, String price)
	{
		final WebElement p = getPeriodPrice(index);
		p.clear();
		p.sendKeys(price);
		return this;
	}

	public String getPrice(int index)
	{
		return getPeriodPrice(index).getAttribute("value");
	}

	public ShowTiersPage save()
	{
		saveButton.click();
		return new ShowTiersPage(context).get();
	}

	public EditTierPage saveInvalidFields()
	{
		saveButton.click();
		return visibilityWaiter(driver, By.className("ctrlinvalidmessage")).get();
	}

	public ShowTiersPage cancel()
	{
		cancelButton.click();
		return new ShowTiersPage(context).get();
	}

	private WebElement getPeriodCheckbox(int index)
	{
		switch( index )
		{
			case 0:
				return enablePeriod0;
			case 1:
				return enablePeriod1;
			case 2:
				return enablePeriod2;
			case 3:
				return enablePeriod3;
			case 4:
				return enablePeriod4;
			default:
				throw new Error("Index out of range");
		}
	}

	private WebElement getPeriodPrice(int index)
	{
		switch( index )
		{
			case 0:
				return period0;
			case 1:
				return period1;
			case 2:
				return period2;
			case 3:
				return period3;
			case 4:
				return period4;
			default:
				throw new Error("Index out of range");
		}
	}

	public boolean isPeriodInvalid(int index)
	{
		return isPresent(getPeriodPrice(index), By.xpath("./../following-sibling::p[@class='ctrlinvalidmessage']"));

	}

	public void disableAllPeriods()
	{
		for( int x = 0; x < 5; x++ )
		{
			this.enablePeriod(x, false);
		}
	}

	public boolean isNoSubTierErrorPresent()
	{
		return isPresent(By.xpath("//p[text() = normalize-space('At least one subscription period must be selected')]"));
	}

	public boolean isNameInvalid()
	{
		return isPresent(nameField, By.xpath("./../../following-sibling::p[@class='ctrlinvalidmessage']"));
	}
}
