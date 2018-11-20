package com.tle.webtests.pageobject.institution;

import java.util.List;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;

public class SelectDbDialog<T extends DbSelectable<T>> extends AbstractPage<SelectDbDialog<T>>
{
	private WebElement getOkButton()
	{
		return findBySectionId("_selectDatabaseDialog_ok");
	}

	private WebElement findBySectionId(String postfix)
	{
		return findWithId(getSectionId(), postfix);
	}

	private WebElement getDbList()
	{
		return findBySectionId("_selectDatabaseDialog_list");
	}

	private final String prefix;
	private final Random random = new Random();
	private T parent;

	public SelectDbDialog(T parent, String prefix)
	{
		super(parent.getContext());
		this.parent = parent;
		this.prefix = prefix;
	}

	public String getSectionId()
	{
		return prefix;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return getOkButton();
	}

	public T selectByIndex(int index)
	{
		getDbList().findElement(By.xpath("./li[" + index + "]/input")).click();
		WaitingPageObject<T> updateWaiter = parent.getUpdateWaiter();
		getOkButton().click();
		return updateWaiter.get();
	}

	public T selectFirst()
	{
		return selectByIndex(1);
	}

	public T selectRandom()
	{
		List<WebElement> checks = getDbList().findElements(By.tagName("li"));
		return selectByIndex(random.nextInt(checks.size()) + 1);
	}
}
