package com.tle.webtests.pageobject.payment.storefront;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.UndeterminedPage;

public class ShopPage extends AbstractPage<ShopPage>
{
	@FindBy(id = "scb_viewCartButton")
	private WebElement viewCartButton;

	@FindBy(id = "col1")
	private WebElement mainDiv;

	@FindBy(id = "sosb_box")
	private WebElement orderBox;
	@FindBy(id = "soab_box")
	private WebElement apprBox;
	@FindBy(id = "sopb_box")
	private WebElement payBox;

	public ShopPage(PageContext context)
	{
		super(context, By.className("shop-layout"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/shop/shop.do");
	}

	public String getStoreImageSrc(String storeName)
	{
		WebElement image = mainDiv.findElement(By.xpath(".//img[@title = " + quoteXPath(storeName) + "]"));
		return image.getAttribute("src");
	}

	@Deprecated
	public BrowseCataloguePage pickStoreSingleCatalogue(String storeName)
	{
		return pickStoreSingleCatalogue(storeName, "cat1");
		// Chances are if it's not specified then this is the one you're after,
		// but tests need to be changed to use the method below instead
	}

	/**
	 * Use this when you're expecting only one catalogue and an immediate
	 * redirect, if it doesn't happen it's ok, we'll handle it for you :)
	 */
	public BrowseCataloguePage pickStoreSingleCatalogue(String storeName, PrefixedName catalogueName)
	{
		return pickStoreSingleCatalogue(storeName, catalogueName.toString());
	}

	private BrowseCataloguePage pickStoreSingleCatalogue(String storeName, String catalogueName)
	{
		mainDiv.findElement(By.xpath(".//img[@title = " + quoteXPath(storeName) + "]")).click();
		ShopCataloguesPage scp = new ShopCataloguesPage(context, storeName);
		BrowseCataloguePage bcp = new BrowseCataloguePage(context, storeName, catalogueName);

		UndeterminedPage<PageObject> undetermined = new UndeterminedPage<PageObject>(context, scp, bcp);

		PageObject po = undetermined.get();

		if( po == scp )
		{
			bcp = scp.browseCatalogue(catalogueName).get();
		}
		return bcp;
	}

	public ShopCataloguesPage pickStore(String storeName)
	{
		mainDiv.findElement(By.xpath(".//img[@title = " + quoteXPath(storeName) + "]")).click();
		return new ShopCataloguesPage(context, storeName).get();
	}

	public OrderPage selectOrderForApproval(String purchaser)
	{
		apprBox
			.findElement(By.xpath("div[@class='box_content']//td/a[contains(text()," + quoteXPath(purchaser) + ")]"))
			.click();
		return new OrderPage(context).get();

	}

	// Used index b/c there's no other unique identifier for an order
	public OrderPage selectOrder(int index)
	{
		orderBox.findElement(By.xpath("div[@class='box_content']//td/a[" + index + "]")).click();
		return new OrderPage(context).get();

	}

	public OrderPage selectOrderForPayment(String purchaser)
	{
		payBox.findElement(By.xpath("div[@class='box_content']//td/a[contains(text()," + quoteXPath(purchaser) + ")]"))
			.click();
		return new OrderPage(context).get();
	}

	public String getTotalForOrder(String purchaser)
	{
		return apprBox.findElement(
			By.xpath("div[@class='box_content']//td/a[contains(text(), " + quoteXPath(purchaser)
				+ ")]/../following-sibling::td")).getText();

	}

	public CartViewPage viewCart()
	{
		viewCartButton.click();
		return new CartViewPage(context).get();
	}

	public boolean hasCheckout()
	{
		return isPresent(viewCartButton);
	}

	public boolean isOrderPresent(int index)
	{
		return isPresent(orderBox, By.xpath("div[@class='box_content']//td/a[" + index + "]"));
	}
}
