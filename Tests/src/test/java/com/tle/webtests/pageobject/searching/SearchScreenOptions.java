package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.WaitingPageObject;

public class SearchScreenOptions extends AbstractSearchPageScreenOptions<SearchScreenOptions>
{
	@FindBy(id = "nonlive")
	private WebElement includeNonLive;
	private AbstractResultList<?, ?> resultList;

	public SearchScreenOptions(AbstractResultList<?, ?> resultList)
	{
		super(resultList.getContext());
		this.resultList = resultList;
	}

	public boolean hasNonLiveOption()
	{
		return isPresent(By.id("nonlive"));
	}

	public SearchScreenOptions setNonLiveOption(boolean nonLive)
	{
		if( includeNonLive.isSelected() != nonLive )
		{
			WaitingPageObject<?> waiter = resultList.getUpdateWaiter();
			includeNonLive.click();
			waiter.get();
		}
		return this;
	}
}
