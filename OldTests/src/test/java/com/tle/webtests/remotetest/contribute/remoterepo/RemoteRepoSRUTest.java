package com.tle.webtests.remotetest.contribute.remoterepo;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.google.common.primitives.Ints;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoListPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoSearchResult;
import com.tle.webtests.pageobject.remoterepo.sru.RemoteRepoSRUSearchPage;
import com.tle.webtests.pageobject.remoterepo.sru.RemoteRepoViewSRUResultPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupAutoTest;

@TestInstitution("fiveo")
public class RemoteRepoSRUTest extends AbstractCleanupAutoTest
{
	private static final String SRU = "SRU";
	private static final int MAX_PAGES = 5;


	@Override
	protected boolean isCleanupItems()
	{
		return false;
	}
	
	@Test
	public void testSRUSearchAndContribute()
	{
		// Via search
		SearchPage sp = new SearchPage(context).load();
		RemoteRepoPage rrp = sp.searchOtherRepositories();

		assertTrue(rrp.isRemoteRepositoryVisible(SRU));

		RemoteRepoSRUSearchPage rrSRU = rrp.clickRemoteRepository(SRU, new RemoteRepoSRUSearchPage(context));
		rrSRU.blankQuery();

		String query = "§¶ß»‹›«‘’“”‚&lt;";
		rrSRU.setQuery(query);
		rrSRU.searchErrorOnPage();

		query = "Java programming for dummies /";
		RemoteRepoListPage searchResults = rrSRU.exactQuery(query);

		assertTrue(searchResults.doesResultExist(query, 1));

		RemoteRepoSearchResult sr = searchResults.getResultForTitle(query, 1);
		RemoteRepoViewSRUResultPage srwViewResult = sr.viewResult(new RemoteRepoViewSRUResultPage(context));

		WizardPageTab contribution = srwViewResult.importResult();
		String text = contribution.getControl(1).getAttribute("value");
		contribution.editbox(1, "RemoteRepoSRUTest - " + text);
		contribution.save().publish();
	}

	@Test
	public void testSRUPaging()
	{
		// Via contribute
		MenuSection ms = new MenuSection(context).get();
		ContributePage contribPage = ms.clickMenu("Contribute", new ContributePage(context));

		assertTrue(contribPage.hasRemoteRepo(SRU));

		RemoteRepoSRUSearchPage rrSRU = contribPage.openRemoteRepo(SRU, new RemoteRepoSRUSearchPage(context));
		rrSRU.search("Java programming");

		if( rrSRU.hasPaging() )
		{
			String stats = rrSRU.getStats();
			String[] split = stats.split(" ");
			int perPage = (Integer.parseInt(split[0]) + Integer.parseInt(split[2])) - 1;
			int total = Integer.parseInt(split[4]);
			int currentPage = rrSRU.getCurrentPage();
			int pages = total / perPage;

			for( int i = currentPage + 1; i <= Ints.min(pages, MAX_PAGES); i++ )
			{
				rrSRU.clickPaging(i);
				assertTrue(rrSRU.onPage(i));
			}
		}
	}

	@Test
	public void testSRUBreadcrumbs()
	{
		ContributePage contribPage = new ContributePage(context).load();
		RemoteRepoSRUSearchPage rrSRU = contribPage.openRemoteRepo(SRU, new RemoteRepoSRUSearchPage(context));

		String query = "Java programming for dummies /";
		RemoteRepoListPage searchResults = rrSRU.exactQuery(query);
		assertTrue(searchResults.doesResultExist(query, 1));

		RemoteRepoSearchResult sr = searchResults.getResultForTitle(query, 1);
		RemoteRepoViewSRUResultPage srwViewResult = sr.viewResult(new RemoteRepoViewSRUResultPage(context));

		rrSRU = srwViewResult.clickRemoteRepoBreadcrumb(SRU);
		assertTrue(rrSRU.results().doesResultExist(query, 1));

		contribPage = rrSRU.clickContributeBreadcrumb();
		assertTrue(contribPage.hasRemoteRepo(SRU));
	}

}
