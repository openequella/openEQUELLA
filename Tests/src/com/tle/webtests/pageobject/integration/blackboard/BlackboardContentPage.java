package com.tle.webtests.pageobject.integration.blackboard;

import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.selection.SelectionSession;

public class BlackboardContentPage extends AbstractBlackboardCoursePage<BlackboardContentPage>
{
	private String mainWindow;
	private boolean popupViews = true;

	public BlackboardContentPage(PageContext context, String courseName, String title)
	{
		super(context, courseName, title);
	}

	public boolean hasResource(String name)
	{
		return isPresent(getRowForItemNameBy(name));
	}

	public boolean hasAttachment(String linkName)
	{
		return isPresent(By.linkText(linkName));
	}

	private WebElement getRowForItemName(String name)
	{
		return driver.findElement(getRowForItemNameBy(name));
	}

	private By getRowForItemNameBy(String name)
	{
		return By.xpath("//div[./h3//span[text()=" + quoteXPath(name) + "]]");
	}

	private WebElement getRowForPartialName(String name)
	{
		return driver.findElement(getRowForPartialNameBy(name));
	}

	private By getRowForPartialNameBy(String name)
	{
		return By.xpath("//div[./h3//span[contains(text(), " + quoteXPath(name) + ")]]");
	}

	public BlackboardContentPage deleteResource(String fullName)
	{
		clickCommandForItem(fullName, "Delete");
		acceptConfirmation();
		return waitForMsg();
	}

	public BlackboardContentPage deleteResourceIfExists(String fullName)
	{
		if( hasResource(fullName) )
		{
			return deleteResource(fullName);
		}
		return get();
	}

	public void deleteResources(String partialName)
	{
		boolean elements = true;
		while( elements )
		{
			try
			{
				clickCommandForElem(getRowForPartialName(partialName), "Delete");
				acceptConfirmation();
				waitForMsg();
			}
			catch( NotFoundException e )
			{
				elements = false;
			}
		}
	}

	public SelectionSession addEquellaResource()
	{
		WebElement actionBar = waitForElement(By.id("actionbar"));
		waitForElement(actionBar, By.linkText("Tools")).click();
		waitForElement(driver, By.linkText("EQUELLA Object")).click();
		return new SelectionSession(context);
	}

	public BlackboardContentPage finishAddEquellaResource(SelectionSession selectionSession)
	{
		return selectionSession.finishStructured(this);
	}

	public BlackboardContentPage addFolder(String name)
	{
		waitForElement(driver, By.linkText("Build Content")).click();
		waitForElement(driver, By.linkText("Content Folder")).click();
		waitForElement(driver, By.id("user_title")).sendKeys(name);
		waitForElement(driver, By.name("bottom_Submit")).click();
		return visibilityWaiter(driver, By.linkText(name)).get();
	}

	public BlackboardEditItemPage editResource(String itemName)
	{
		clickCommandForItem(itemName, "Edit");
		return new BlackboardEditItemPage(context, this).get();
	}

	public BlackboardContentPage moveResource(String itemName, String course, String location)
	{
		clickCommandForItem(itemName, "Move");
		BlackboardMoveCopyItemPage moveItemPage = new BlackboardMoveCopyItemPage(context, this, "Move").get();
		moveItemPage.setCourse(course);
		moveItemPage.setDestination(location);
		return moveItemPage.submit();
	}

	public BlackboardContentPage copyResource(String itemName, String course, String location)
	{
		clickCommandForItem(itemName, "Copy");
		BlackboardMoveCopyItemPage moveItemPage = new BlackboardMoveCopyItemPage(context, this, "Copy").get();
		moveItemPage.setCourse(course);
		moveItemPage.setDestination(location);
		return moveItemPage.submit();
	}

	protected void clickCommandForItem(String itemName, String command)
	{
		WebElement elem = getRowForItemName(itemName);
		elem.findElement(By.xpath("./span[contains(@class, 'contextMenuContainer')]/a")).click();
		waitForElement(By.xpath("//body/div[contains(@class, 'cmdiv')]"));
		driver.findElement(By.xpath("//body/div[contains(@class, 'cmdiv')]")).findElement(By.linkText(command)).click();
	}

	protected void clickCommandForElem(WebElement elem, String command)
	{
		elem.findElement(By.xpath("./span[contains(@class, 'contextMenuContainer')]/a")).click();
		waitForElement(By.xpath("//body/div[contains(@class, 'cmdiv')]"));
		driver.findElement(By.xpath("//body/div[contains(@class, 'cmdiv')]")).findElement(By.linkText(command)).click();
	}

	public <T extends AbstractPage<T>> T viewResource(String itemName, T page)
	{
		driver.findElement(By.linkText(itemName)).click();
		return openContent(page);
	}

	public void closePopup()
	{
		if( popupViews )
		{
			driver.close();
			driver.switchTo().window(mainWindow);
			waiter.until(ExpectedConditions.visibilityOfElementLocated(By.id("contentPanel")));
		}
	}

	public BlackboardContentPage returnFromResource(String folderName)
	{
		if( popupViews )
		{
			return closeContent(new BlackboardContentPage(context, courseName, folderName));
		}
		//Doesn't work in Firefox, (re-submits LTI launch), hence this whole code branch doesn't work :(
		//context.getDriver().navigate().back();
		return new BlackboardContentPage(context, courseName, folderName);
	}

	public BlackboardContentPage enterFolder(String folder)
	{
		driver.findElement(By.linkText(folder)).click();
		return new BlackboardContentPage(context, courseName, folder);
	}

	private BlackboardContentPage waitForMsg()
	{
		waitForElement(By.id("goodMsg1"));
		driver.findElement(By.xpath("id('inlineReceipt_good')//a")).click();
		return get();
	}

	private <T extends AbstractPage<T>> T openContent(T pain)
	{
		if( popupViews )
		{
			final String currentWindowHandle = driver.getWindowHandle();
			mainWindow = currentWindowHandle;
			waiter.until(new ExpectedCondition<Boolean>()
			{
				@Override
				public Boolean apply(WebDriver d)
				{
					Set<String> windowList = driver.getWindowHandles();
					for( String windowHandle : windowList )
					{
						if( !Check.isEmpty(windowHandle) && !windowHandle.equals(currentWindowHandle) )
						{
							driver.switchTo().window(windowHandle);
							return Boolean.TRUE;
						}
					}
					return Boolean.FALSE;
				}
			});
		}
		return pain.get();
	}

	private <T extends AbstractPage<T>> T closeContent(T pain)
	{
		closePopup();
		return pain.get();
	}

}
