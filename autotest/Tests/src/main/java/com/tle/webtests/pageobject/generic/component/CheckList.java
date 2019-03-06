package com.tle.webtests.pageobject.generic.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.searching.ItemListPage;

public class CheckList extends AbstractPage<CheckList> implements ListRenderer
{

	private final String id;

	public CheckList(PageContext context, String id)
	{
		super(context, By.name(id));
		this.id = id;
	}

	@Override
	public void selectAll()
	{
		WebElement checkListContainer = driver.findElement(By.id(id));
		List<WebElement> checks = checkListContainer.findElements(By.xpath(".//input"));
		for( WebElement check : checks )
		{
			if( Check.isEmpty(check.getAttribute("checked")) )
			{
				check.click();
			}

		}
	}

	@Override
	public void setSelectionByText(String... names)
	{
		Set<String> nameSet = new HashSet<String>(Arrays.asList(names));
		WebElement checkListContainer = driver.findElement(By.id(id));
		List<WebElement> checks = checkListContainer.findElements(By.xpath(".//input"));
		for( WebElement check : checks )
		{
			boolean shouldCheck = nameSet.contains(checkListContainer.findElement(
				By.xpath(".//label[@for=" + quoteXPath(check.getAttribute("id")) + "]")).getText());
			if( check.isSelected() != shouldCheck )
			{
				Actions actions = new Actions(driver);
				actions.click(check);
				actions.perform();
			}
		}
	}

	@Override
	public void setSelectionByValue(String... values)
	{
		Set<String> valueSet = new HashSet<String>(Arrays.asList(values));
		WebElement checkListContainer = driver.findElement(By.id(id));
		List<WebElement> checks = checkListContainer.findElements(By.xpath(".//input"));
		for( WebElement check : checks )
		{
			boolean shouldCheck = valueSet.contains(check.getAttribute("value"));
			if( check.isSelected() != shouldCheck )
			{
				Actions actions = new Actions(driver);
				actions.click(check);
				actions.perform();
			}
		}
	}

	public void setSelectionByText(ItemListPage page, String... names)
	{
		Set<String> nameSet = new HashSet<String>(Arrays.asList(names));
		WebElement checkListContainer = driver.findElement(By.id(id));
		List<WebElement> checks = checkListContainer.findElements(By.xpath(".//input"));
		for( WebElement check : checks )
		{
			boolean shouldCheck = nameSet.contains(driver.findElement(
				By.xpath("//label[@for=" + quoteXPath(check.getAttribute("id")) + "]")).getText());
			if( check.isSelected() != shouldCheck )
			{
				WaitingPageObject<ItemListPage> updateWaiter = page.getUpdateWaiter();
				Actions actions = new Actions(driver);
				actions.click(check);
				actions.perform();
				updateWaiter.get();
			}
		}
	}

	public List<Pair<String, String>> getSelectionOptions()
	{
		List<Pair<String, String>> selectionOptions = new ArrayList<Pair<String, String>>();
		WebElement checkListContainer = driver.findElement(By.id(id));
		List<WebElement> checks = checkListContainer.findElements(By.xpath(".//input"));
		for( WebElement check : checks )
		{
			String anOptionText = checkListContainer.findElement(
				By.xpath(".//label[@for=" + quoteXPath(check.getAttribute("id")) + "]")).getText();
			String anOptionAlt = checkListContainer.findElement(
				By.xpath(".//label[@for=" + quoteXPath(check.getAttribute("id")) + "]")).getAttribute("title");
			selectionOptions.add(new Pair<String, String>(anOptionText, anOptionAlt));
		}
		return selectionOptions;
	}

	@Override
	public List<String> getSelectedTexts()
	{
		return Lists.transform(getSelectionOptions(), new Function<Pair<String, String>, String>()
		{
			@Override
			public String apply(Pair<String, String> option)
			{
				return option.getFirst();
			}
		});
	}

	@Override
	public List<String> getSelectedValues()
	{
		final List<String> values = Lists.newArrayList();
		WebElement checkListContainer = driver.findElement(By.id(id));
		List<WebElement> checks = checkListContainer.findElements(By.xpath(".//input"));
		for( WebElement check : checks )
		{
			values.add(check.getAttribute("value"));
		}
		return values;
	}
}
