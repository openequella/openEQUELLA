package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.tle.common.Check;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;

public class NavNodeEditor extends NewAbstractWizardControl<NavNodeEditor>
{
	@FindBy(id = "{wizid}optsingle")
	private WebElement singleResource;
	@FindBy(id = "{wizid}optmultiple")
	private WebElement multipleResource;
	@FindBy(id = "{wizid}_al")
	private WebElement resource;
	@FindBy(id = "{wizid}_vl")
	private WebElement viewer;
	@FindBy(id = "{wizid}_nd")
	private WebElement nameField;
	@FindBy(id = "{wizid}_ta")
	private WebElement addResourceButton;
	@FindBy(id = "tabs")
	private WebElement tabsElem;

	private final NavNodePageObject navNode;

	public NavNodeEditor(NavigationBuilder builder, NavNodePageObject navNode)
	{
		super(builder.getContext(), builder.getCtrlNum(), builder.getPage());
		this.navNode = navNode;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return nameField;
	}

	@Override
	protected void checkLoadedElement()
	{
		super.checkLoadedElement();
		if( !loadedElement.getAttribute("value").equals(navNode.getDisplayName()) )
		{
			throw new Error("Name field not updated");
		}
	}

	public void setFields(String newTitle, String resourceName, String viewerName)
	{
		if( !Check.isEmpty(newTitle) )
		{
			nameField.clear();
			nameField.sendKeys(newTitle);
			navNode.setDisplayName(newTitle);
		}
		if( !Check.isEmpty(resourceName) )
		{
			new EquellaSelect(context, resource).selectByVisibleText(resourceName);
		}
		if( !Check.isEmpty(viewerName) )
		{
			new EquellaSelect(context, viewer).selectByVisibleText(viewerName);
		}
	}

	public void setMultiple(boolean multiple)
	{
		if( multipleResource.isSelected() != multiple )
		{
			ExpectWaiter<NavNodeEditor> waiter;
			if( multiple )
			{
				waiter = ExpectWaiter.waiter(ExpectedConditions.visibilityOf(tabsElem), this);
				multipleResource.click();
			}
			else
			{
				waiter = ExpectWaiter.waiter(ExpectedConditions2.invisibilityOf(tabsElem), this);
				singleResource.click();
			}
			waiter.get();
		}
	}

	public AddTabDialog addTab()
	{
		addResourceButton.click();
		return new AddTabDialog(this).get();
	}

	public static class AddTabDialog extends NewAbstractWizardControl<AddTabDialog>
	{
		@FindBy(id = "{wizid}_tabDialog_tal")
		private WebElement tabResource;
		@FindBy(id = "{wizid}_tabDialog_tvl")
		private WebElement tabViewer;
		@FindBy(id = "{wizid}_tabDialog_ptn")
		private WebElement tabNameField;
		@FindBy(id = "{wizid}_tabDialog_ok")
		private WebElement tabSave;
		private final NavNodeEditor navEditor;

		public AddTabDialog(NavNodeEditor navEditor)
		{
			super(navEditor.getContext(), navEditor.getCtrlNum(), navEditor.getPage());
			this.navEditor = navEditor;
		}

		@Override
		protected WebElement findLoadedElement()
		{
			return tabNameField;
		}

		public void setResource(String resource)
		{
			new EquellaSelect(context, tabResource).selectByVisibleText(resource);
		}

		public void setTabName(String tabName)
		{
			tabNameField.clear();
			tabNameField.sendKeys(tabName);
		}

		public NavNodeEditor save()
		{
			WaitingPageObject<NavNodeEditor> waiter = navEditor.getTabWaiter(tabNameField.getAttribute("value"));
			tabSave.click();
			return waiter.get();
		}
	}

	public WaitingPageObject<NavNodeEditor> getTabWaiter(String tabName)
	{
		return ExpectWaiter.waiter(
			ExpectedConditions2.visibilityOfElementLocated(tabsElem,
				By.xpath("li[a/text()=" + quoteXPath(tabName) + "]")), this);
	}
}
