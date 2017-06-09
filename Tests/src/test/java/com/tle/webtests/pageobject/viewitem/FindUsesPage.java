package com.tle.webtests.pageobject.viewitem;

import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;

public class FindUsesPage extends ItemPage<FindUsesPage>
{
	@FindBy(id = "fuc_cl")
	private WebElement connector;
	@FindBy(id = "fuc_usefil")
	private WebElement filterBox;
	@FindBy(id = "fuc_use")
	private WebElement usesTable;
	@FindBy(id = "lms-table-ajax")
	private WebElement ajaxDiv;
	@FindBy(xpath = "id('lms-table-ajax')//h3[text()='Where this resource is used']")
	private WebElement updateTitle;
	@FindBy(xpath = "//h2[text()='Find uses']")
	private WebElement findUsesTitle;

	private FindUsesTable findUsesTable;

	public FindUsesPage(PageContext context)
	{
		super(context);
		findUsesTable = new FindUsesTable(this);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return findUsesTitle;
	}

	public WaitingPageObject<FindUsesPage> updateTable()
	{
		return ajaxUpdateExpect(ajaxDiv, updateTitle);
	}

	public boolean singleConnector()
	{
		return !isPresent(By.id("fuc_cl"));
	}

	public FindUsesPage selectConnector(PrefixedName name)
	{
		new EquellaSelect(context, connector).selectByVisibleText(name.toString());
		findUsesTable.get();
		return this;
	}

	public boolean hasFilterBox()
	{
		return isVisible(filterBox);
	}

	public FindUsesPage filterUses(String query, String course, String change, boolean appear)
	{
		ExpectedCondition<?> cond;
		By xpath = entryXPath(course, change);
		if( appear )
		{
			cond = ExpectedConditions2.visibilityOfElementLocated(usesTable, xpath);
		}
		else
		{
			cond = ExpectedConditions2.invisibilityOfElementLocated(usesTable, xpath);
		}
		filterBox.clear();
		filterBox.sendKeys(query);
		filterBox.sendKeys(Keys.ENTER);
		waiter.until(cond);
		return this;
	}

	public boolean hasEntry(String course, String location)
	{
		return isVisible(entryXPath(course, location));
	}

	private By entryXPath(String course, String location)
	{
		return By.xpath("id('fuc_use')//td/a[text()=" + quoteXPath(course)
			+ "]/../../td[normalize-space(descendant::text())=" + quoteXPath(location) + "]");
	}

	/**
	 * @param position 1 based
	 * @param course
	 * @param location
	 * @return
	 */
	public boolean hasEntryAt(int position, String course, String location)
	{
		return isVisible(By.xpath("id('fuc_use')//tbody/tr[not(contains(@class, 'rowHidden'))][" + position
			+ "]/td/a[text()=" + quoteXPath(course) + "]/../../td[normalize-space(descendant::text())="
			+ quoteXPath(location) + "]"));
	}

	// public boolean hasAttachment(String course, String location, String
	// attachmentName)
	// {
	// return isVisible(By
	// .xpath("id('detailsTable')//td/a[text()="
	// + quoteXPath(course)
	// + "]/../../td[text()="
	// + quoteXPath(location)
	// +
	// "]/../td//b[normalize-space(text())='Attachment:'][normalize-space(following-sibling::text())="
	// + quoteXPath(attachmentName) + "]"));
	// }

	public boolean hasAttachment(String course, String location, String attachmentName)
	{
		return hasDetail(course, location, "Attachment:", attachmentName);
	}

	public boolean hasDetail(String course, String location, String key, String value)
	{
		return hasDetail(course, location, 1, key, value);
	}

	public boolean hasDetail(String course, String location, int index, String key, String value)
	{
		List<WebElement> elements = driver.findElements(By.xpath("id('fuc_use')//td/a[text()=" + quoteXPath(course)
			+ "]/../../td[descendant::text()=" + quoteXPath(location) + "]"));

		if( elements.size() < index )
		{
			throw new NoSuchElementException();
		}
		elements.get(index - 1).click();
		waitForElement(elements.get(index - 1).findElement(By.xpath("..//div[@class='itemDetails']")));

		By xpath = By.xpath("//div[@class='itemDetails']/div[normalize-space(text())=" + quoteXPath(value)
			+ "]/b[normalize-space(text())=" + quoteXPath(key) + "]");
		return isVisible(xpath);
	}

	public FindUsesPage showAllVersions(boolean on)
	{
		return findUsesTable.get().showAllVersions(on);
	}

	public String getSort()
	{
		return usesTable.findElement(By.xpath(".//th[not(contains(@class, 'unsorted'))]")).getText();
	}

	public boolean isAscendingSort()
	{
		return usesTable.findElement(By.xpath(".//th[not(contains(@class, 'unsorted'))]")).getAttribute("class")
			.contains("sortedasc");
	}

	public void sortBy(String column)
	{
		usesTable.findElement(By.xpath(".//th[normalize-space(text())=" + quoteXPath(column) + "]")).click();
	}

	public void sortBy(String column, boolean asc)
	{
		if( !getSort().equals(column) )
		{
			sortBy(column);
		}
		if( isAscendingSort() != asc )
		{
			sortBy(column);
		}
	}

	public FindUsesPage showArchived(boolean on)
	{
		return findUsesTable.get().showArchived(on);
	}

	public WebElement getTableAjaxElement()
	{
		return ajaxDiv;
	}
}
