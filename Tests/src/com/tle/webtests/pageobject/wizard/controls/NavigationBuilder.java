package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class NavigationBuilder extends AbstractWizardControl<NavigationBuilder>
{
	@FindBy(id = "{wizid}_pp")
	private WebElement initButton;
	@FindBy(id = "{wizid}_ac")
	private WebElement addChildButton;
	@FindBy(id = "{wizid}_as")
	private WebElement addSiblingButton;
	@FindBy(id = "{wizid}_r")
	private WebElement removeButton;
	@FindBy(id = "{wizid}_mu")
	private WebElement moveUpButton;
	@FindBy(id = "{wizid}_md")
	private WebElement moveDownButton;
	@FindBy(id = "{wizid}_mn")
	private WebElement showOthersCheck;
	@FindBy(id = "{wizid}_ss")
	private WebElement showSplitCheck;

	@FindBy(id = "root")
	private WebElement rootDiv;

	public NavigationBuilder(PageContext context, AbstractWizardControlPage<?> page)
	{
		super(context, 1, page);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return initButton;
	}

	public NavigationBuilder initialiseNavigation(boolean wasBlank)
	{
		WaitingPageObject<NavigationBuilder> waiter = updateWaiter();
		initButton.click();
		if( !wasBlank )
		{
			acceptConfirmation();
		}
		return waiter.get();
	}

	public int nodeCount()
	{
		return rootDiv.findElements(By.xpath(".//div[@class='navNode']")).size();
	}

	public boolean nodeExists(String title)
	{
		return isPresent(rootDiv, By.xpath(".//a[text()=" + quoteXPath(title) + "]"));
	}

	private NavNodePageObject selectNode(String title)
	{
		NavNodePageObject navNode = new NavNodePageObject(this, rootDiv, title);
		navNode.setAnyChild(true);
		navNode = navNode.get();
		navNode.select();
		return navNode;
	}

	public void removeNode(String title)
	{
		NavNodePageObject node = selectNode(title);
		WaitingPageObject<NavigationBuilder> removalWaiter = node.removalWaiter();
		removeButton.click();
		removalWaiter.get();
	}

	public NavNodePageObject addChild(String target, String newTitle, String resourceName)
	{
		return addChild(target, newTitle, resourceName, "");
	}

	public NavNodePageObject addChild(String target, String newTitle, String resourceName, String viewerName)
	{
		NavNodePageObject parentNode = selectNode(target);
		NavNodePageObject newChild = parentNode.newChild();
		addChildButton.click();
		newChild.get().select().setFields(newTitle, resourceName, viewerName);
		return newChild;
	}

	public NavNodePageObject addSibling(String target, String newTitle, String resourceName)
	{
		return addSibling(target, newTitle, resourceName, "");
	}

	public NavNodePageObject addSibling(String target, String newTitle, String resourceName, String viewerName)
	{
		NavNodePageObject sibling = selectNode(target);
		NavNodePageObject newSibling = sibling.newSibling();
		addSiblingButton.click();
		newSibling.get().select().setFields(newTitle, resourceName, viewerName);
		return newSibling;
	}

	public NavNodePageObject addTopLevelNode(String displayName, String resourceName)
	{
		NavNodePageObject newSibling = new NavNodePageObject(this, rootDiv, "New Node");
		addSiblingButton.click();
		newSibling.get().select().setFields(displayName, resourceName, "");
		return newSibling;
	}

	public void moveUp(String target)
	{
		NavNodePageObject navNode = selectNode(target);
		WaitingPageObject<NavNodePageObject> waiter = navNode.moveWaiter();
		moveUpButton.click();
		waiter.get();
	}

	public void moveDown(String target)
	{
		NavNodePageObject navNode = selectNode(target);
		WaitingPageObject<NavNodePageObject> waiter = navNode.moveWaiter();
		moveDownButton.click();
		waiter.get();
	}

	public void dragToBefore(String moveThisNode, String beforeNode)
	{
		// FIXME when we can drag again

		// String xp =
		// ".//div[contains(@class, 'droppable') and contains(@class, 'before')]";
		// if( Check.isEmpty(beforeNode) )
		// {
		// xp += "[0]";
		// }
		// else
		// {
		// xp += "[following-sibling::div/div/span/a[text()=" +
		// quoteXPath(beforeNode) + "]]";
		// }

		// new Actions(driver).dragAndDrop(getNodeElement(moveThisNode),
		// rootDiv.findElement(By.xpath(xp))).perform();
	}

	public void setShowOtherAttachments(boolean check)
	{
		if( showOthersCheck.isSelected() != check )
		{
			showOthersCheck.click();
		}
	}

	public void setSplitView(boolean check)
	{
		if( showSplitCheck.isSelected() != check )
		{
			showSplitCheck.click();
		}
	}
}
