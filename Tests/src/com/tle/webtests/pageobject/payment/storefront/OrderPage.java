package com.tle.webtests.pageobject.payment.storefront;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

// FIXME: this should be a subclass of CartViewPage!!
/**
 * This should cover the 'Order' page,'Order approval' page and the 'Order
 * payment' page. Not all controls will be on all pages.
 */

public class OrderPage extends AbstractPage<OrderPage>
{
	@FindBy(id = "svc_c")
	private WebElement commentBox;
	@FindBy(id = "svc_submitButton")
	private WebElement submitButton;
	@FindBy(id = "svc_deleteButton")
	private WebElement deleteButton;
	@FindBy(id = "svc_rejectButton")
	private WebElement rejectButton;
	@FindBy(id = "svc_approveButton")
	private WebElement approveButton;
	@FindBy(id = "svc_redraftButton")
	private WebElement redraftButton;

	@FindBy(xpath = "//div[@class='shopStatus']/h3/strong")
	private WebElement orderStatus;

	public OrderPage(PageContext context)
	{
		super(context, By.xpath("//div[contains(@class,'banner')][contains(text(),'Order')]"));
	}

	public OrderPage addComment(String comment)
	{
		commentBox.clear();
		commentBox.sendKeys(comment);
		return this;
	}

	public OrderPage submitOrder()
	{
		return clickAndRemove(submitButton);
	}

	public ShopPage deleteOrder()
	{
		deleteButton.click();
		driver.switchTo().alert().accept();
		return new ShopPage(context).get();
	}

	public ShopPage rejectOrder()
	{
		rejectButton.click();
		return new ShopPage(context).get();
	}

	public ShopPage approveOrder()
	{
		approveButton.click();
		return new ShopPage(context).get();
	}

	public CartViewPage redraftOrder()
	{
		redraftButton.click();
		return new CartViewPage(context).get();
		// it's the same type of page but will look different
	}

	private WebElement getComment(int index)
	{
		return driver.findElement(By.xpath("//div[@class = 'orderhistories']/div[" + index + "]"));
	}

	public String getCommentText(int index)
	{
		WebElement comment = getComment(index);
		return comment.findElement(By.xpath(".//div[@class='orderhistory-content']/p")).getText();
	}

	public boolean commentExists(int index)
	{
		return isPresent(By.xpath("//div[@class = 'orderhistories']/div[" + index + "]"));
		// According to the wiki we shouldn't catch a NoSuchElementException,
		// this is where we maybe could/should?
	}

	public String getCommentType(int index)
	{
		WebElement comment = getComment(index).findElement(
			By.xpath(".//div[1]/span[contains(@class,'orderhistory-description')]"));
		String title = comment.getText();
		return title;
	}

	public String getOrderStatus()
	{
		return orderStatus.getText();
	}

	public OrderPage waitForStatus(final String status)
	{
		waiter.until(new ExpectedCondition<Boolean>()
		{
			@Override
			public Boolean apply(WebDriver arg0)
			{
				driver.navigate().refresh();
				get();
				return status.equals(orderStatus.getText());
			}

		});
		return get();
	}

}
