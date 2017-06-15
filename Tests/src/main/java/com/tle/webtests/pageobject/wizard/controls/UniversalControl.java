package com.tle.webtests.pageobject.wizard.controls;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import com.tle.webtests.pageobject.wizard.controls.universal.AttachmentEditPage;
import com.tle.webtests.pageobject.wizard.controls.universal.AttachmentType;
import com.tle.webtests.pageobject.wizard.controls.universal.PickAttachmentTypeDialog;

public class UniversalControl extends NewAbstractWizardControl<UniversalControl>
{
	private static final By BY_ALLRESOURCES = By.xpath(".//tr[td[contains(@class, 'name')]]");
	@FindBy(id = "{wizid}_addLink")
	private WebElement addResourceButton;
	@FindBy(id = "{wizid}universalresources")
	private WebElement rootElem;

	public UniversalControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
	}

	public int getCtrlNum()
	{
		return ctrlnum;
	}

	public AbstractWizardControlPage<?> getPage()
	{
		return page;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return rootElem;
	}

	public <T extends AttachmentType<T, ?>> T addDefaultResource(T type)
	{
		addResourceButton.click();
		return type.get();
	}

	/**
	 * @param <T>
	 * @param type
	 * @return
	 */
	public <T extends AttachmentType<T, ?>> T addResource(T type)
	{
		addResourceButton.click();
		PickAttachmentTypeDialog dialog = new PickAttachmentTypeDialog(this).get();
		return dialog.clickType(type);
	}

	public ExpectedCondition<WebElement> getResourceExpectation(String title)
	{
		return getResourceExpectation(title, false);
	}

	public ExpectedCondition<WebElement> getResourceExpectation(String title, boolean disabled)
	{
		return ExpectedConditions2.visibilityOfElementLocated(rootElem, getRowByTitle(title, disabled));
	}

	public ExpectedCondition<Boolean> getResourceGoneExpectation(String title)
	{
		return getResourceGoneExpectation(title, false);
	}

	public ExpectedCondition<Boolean> getResourceGoneExpectation(String title, boolean wasDisabled)
	{
		return ExpectedConditions.stalenessOf(((WrapsElement) rootElem.findElement(getRowByTitle(title, wasDisabled)))
			.getWrappedElement());
	}

	private By getRowByTitle(String title)
	{
		return getRowByTitle(title, false);
	}

	private By getRowByTitle(String title, boolean disabled)
	{
		return By.xpath(".//tr[.//" + (disabled ? "span" : "a") + "[text()=" + quoteXPath(title) + "]]");
	}

	private By getActionLink(String action)
	{
		return By.xpath("td[@class='actions']/a[text()=" + quoteXPath(action) + "]");
	}

	private void clickAction(By rowBy, String action)
	{
		rootElem.findElement(new ByChained(rowBy, getActionLink(action))).click();
	}

	public <P extends AttachmentEditPage, T extends AttachmentType<T, P>> P editResource(T type, String title)
	{
		clickAction(getRowByTitle(title), "Edit");
		return type.edit();
	}

	public <P extends AttachmentEditPage> P editResource(WaitingPageObject<P> editor, String title)
	{
		clickAction(getRowByTitle(title), "Edit");
		return editor.get();
	}

	public UniversalControl deleteResource(String title)
	{
		return deleteResource(getRowByTitle(title), attachGoneWaiter(title));
	}

	private UniversalControl deleteResource(By rowBy, WaitingPageObject<UniversalControl> returnTo)
	{
		clickAction(rowBy, "Delete");
		acceptConfirmation();
		return returnTo.get();
	}

	public boolean resourceIsPreview(String title)
	{
		getRowByTitle(title);
		return isPresent(new ByChained(getRowByTitle(title), By.className("preview-tag")));
	}

	public <T extends AttachmentType<T, ?>> T replaceResource(T type, String title)
	{
		clickAction(getRowByTitle(title), "Replace");
		PickAttachmentTypeDialog dialog = new PickAttachmentTypeDialog(this).get();
		dialog.clickType(type);
		return type.get();
	}

	public <T extends AttachmentType<T, ?>> T replaceSingleResource(T type, String title)
	{
		clickAction(getRowByTitle(title), "Replace");
		return type.get();
	}

	public String getAttachmentUuid(String title)
	{
		String url = rootElem.findElement(By.xpath(".//a[text()=" + quoteXPath(title) + "]")).getAttribute("href");

		return url.substring(url.length() - 36);
	}

	public boolean hasResource(String title)
	{
		return isPresent(rootElem, getRowByTitle(title));
	}

	public int resourceCount()
	{
		return rootElem.findElements(BY_ALLRESOURCES).size();
	}

	public WaitingPageObject<UniversalControl> attachNameWaiter(String newName, boolean disabled)
	{
		return ExpectWaiter.waiter(getResourceExpectation(newName, disabled), this);
	}

	public WaitingPageObject<UniversalControl> attachGoneWaiter(String oldName)
	{
		return ExpectWaiter.waiter(getResourceGoneExpectation(oldName), this);
	}

	public WaitingPageObject<UniversalControl> attachmentCountExpectation(int newAttachments)
	{
		ExpectedCondition<List<WebElement>> expectedCondition = ExpectedConditions2.numberOfElementLocated(rootElem,
			BY_ALLRESOURCES, resourceCount() + newAttachments);
		return ExpectWaiter.waiter(expectedCondition, this);
	}
}
