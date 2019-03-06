package com.tle.webtests.pageobject.remoterepo.srw;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.remoterepo.AbstractRemoteRepoSearchPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoListPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoSearchResult;

public class RemoteRepoSRWSearchPage
	extends
		AbstractRemoteRepoSearchPage<RemoteRepoSRWSearchPage, RemoteRepoListPage, RemoteRepoSearchResult>
{
	@FindBy(xpath = "id('searchform')/h2[text()='Searching SRW']")
	private WebElement mainElem;

	public RemoteRepoSRWSearchPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return mainElem;
	}

	@Override
	public RemoteRepoListPage resultsPageObject()
	{
		return new RemoteRepoListPage(context);
	}
}
