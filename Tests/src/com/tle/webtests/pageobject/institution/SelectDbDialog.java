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
	@FindBy(id = "{sectionId}_selectDatabaseDialog_ok")
	private WebElement okButton;
	@FindBy(id = "{sectionId}_selectDatabaseDialog_list")
	private WebElement dbList;

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
		return okButton;
	}

	public T selectByIndex(int index)
	{
		dbList.findElement(By.xpath("./li[" + index + "]/input")).click();
		WaitingPageObject<T> updateWaiter = parent.getUpdateWaiter();
		okButton.click();
		return updateWaiter.get();
	}

	public T selectFirst()
	{
		return selectByIndex(1);
	}

	public T selectRandom()
	{
		List<WebElement> checks = dbList.findElements(By.tagName("li"));
		return selectByIndex(random.nextInt(checks.size()) + 1);
	}
}
