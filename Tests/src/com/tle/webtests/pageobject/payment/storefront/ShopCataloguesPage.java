package com.tle.webtests.pageobject.payment.storefront;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PrefixedName;

public class ShopCataloguesPage extends AbstractPage<ShopCataloguesPage>
{
	private String storeName;

	public ShopCataloguesPage(PageContext context, String storeName)
	{
		super(context, By.xpath("//div[@class = 'area']/h2[text() = \"Catalogues\"]"));
		this.storeName = storeName;
	}

	public int getCountForCatalogue(PrefixedName catalogueName)
	{
		String catName = catalogueName.toString();
		String linkText = driver.findElement(
			By.xpath("//div[@id = 'catalogue_list']/ul/li/a[contains(.," + quoteXPath(catName) + ")]")).getText();
		linkText = linkText.substring(catName.length() + 3, linkText.length() - 2);
		return Integer.parseInt(linkText);
	}

	public boolean catalogueExists(PrefixedName catalogueName)
	{
		return isPresent(By.xpath("//div[@id = 'catalogue_list']/ul/li/a[contains(.,"
			+ quoteXPath(catalogueName.toString()) + ")]"));
	}

	public BrowseCataloguePage browseCatalogue(String catalogueName)
	{
		driver.findElement(
			By.xpath("//div[@id = 'catalogue_list']/ul/li/a[contains(.," + quoteXPath(catalogueName) + ")]")).click();
		return new BrowseCataloguePage(context, storeName, catalogueName).get();
	}
}
