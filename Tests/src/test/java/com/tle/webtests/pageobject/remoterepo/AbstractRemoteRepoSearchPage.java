package com.tle.webtests.pageobject.remoterepo;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.searching.AbstractQueryableSearchPage;
import com.tle.webtests.pageobject.searching.AbstractResultList;
import com.tle.webtests.pageobject.searching.SearchResult;
import com.tle.webtests.pageobject.wizard.ContributePage;

public abstract class AbstractRemoteRepoSearchPage<T extends AbstractRemoteRepoSearchPage<T, RL, SR>, RL extends AbstractResultList<RL, SR>, SR extends SearchResult<SR>>
	extends
		AbstractQueryableSearchPage<T, RL, SR>
{

	public AbstractRemoteRepoSearchPage(PageContext context)
	{
		super(context, 60);
	}

	public ContributePage clickContributeBreadcrumb()
	{
		driver.findElement(By.xpath("id('breadcrumbs')//a[text()='Contribute']")).click();
		return new ContributePage(context).get();
	}
}
