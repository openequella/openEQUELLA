package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.generic.page.AbstractScreenOptions;

public abstract class AbstractSearchPageScreenOptions<T extends AbstractSearchPageScreenOptions<T>>
	extends
		AbstractScreenOptions<T>
{
	private EquellaSelect perPageList;

	public AbstractSearchPageScreenOptions(PageContext context)
	{
		super(context);
	}

	@Override
	public void checkLoaded() throws Error
	{
		super.checkLoaded();
		perPageList = new EquellaSelect(context, driver.findElement(By.id("p_pp")));
	}

	/***
	 * @param perPage - Must be either min, middle, max
	 */
	public T setPerPage(String perPage)
	{
		perPageList.selectByValue(perPage);
		return get();
	}

	public int getPerPage()
	{
		return Integer.parseInt(perPageList.getSelectedValue());
	}
}
