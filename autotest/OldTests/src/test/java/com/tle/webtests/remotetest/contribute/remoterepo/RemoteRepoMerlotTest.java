package com.tle.webtests.remotetest.contribute.remoterepo;

import static org.testng.Assert.assertTrue;

import com.google.common.primitives.Ints;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoListPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoSearchResult;
import com.tle.webtests.pageobject.remoterepo.merlot.RemoteRepoMerlotSearchPage;
import com.tle.webtests.pageobject.remoterepo.merlot.RemoteRepoViewMerlotResultPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupAutoTest;

@TestInstitution("fiveo")
public class RemoteRepoMerlotTest extends AbstractCleanupAutoTest
{
	private static final String MERLOT = "MERLOT";
	private static final int MAX_PAGES = 5;

	// @DataProvider(name = "sortOptions", parallel = false)
	public Object[][] sortOptions()
	{
		return new Object[][]{{"overallRating"}, {"title"}, {"author"}, {"materialtype"}, {"dateCreated"}};
	}

	// @Test
	public void testMerlotSearchAndContribute()
	{
		SearchPage sp = new SearchPage(context).load();
		RemoteRepoPage rrp = sp.searchOtherRepositories();
		String remoteRepo = MERLOT;

		assertTrue(rrp.isRemoteRepositoryVisible(remoteRepo));

		RemoteRepoMerlotSearchPage rrMerlot = rrp.clickRemoteRepository(remoteRepo, new RemoteRepoMerlotSearchPage(
			context));
		String query = "Music Acoustics";
		RemoteRepoListPage searchResults = rrMerlot.exactQuery(query);

		assertTrue(searchResults.doesResultExist(query, 1));

		RemoteRepoSearchResult sr = searchResults.getResultForTitle(query, 1);
		RemoteRepoViewMerlotResultPage merlotViewResult = sr.viewResult(new RemoteRepoViewMerlotResultPage(context));

		WizardPageTab contribution = merlotViewResult.importResult();
		String text = contribution.getControl(1).getAttribute("value");
		contribution.editbox(1, "RemoteRepoMerlotTest - " + text);
		contribution.save().publish();
	}

	// @Test
	public void testMerlotPaging()
	{
		MenuSection ms = new MenuSection(context).get();
		ContributePage contribPage = ms.clickMenu("Contribute", new ContributePage(context)).load();

		assertTrue(contribPage.hasRemoteRepo(MERLOT));

		RemoteRepoMerlotSearchPage rrMerlot = contribPage.openRemoteRepo(MERLOT,
			new RemoteRepoMerlotSearchPage(context));
		rrMerlot.search("Maths");

		if( rrMerlot.hasPaging() )
		{
			String stats = rrMerlot.getStats();
			String[] split = stats.split(" ");
			int perPage = (Integer.parseInt(split[1]) + Integer.parseInt(split[3])) - 1;
			int total = Integer.parseInt(split[5]);
			int currentPage = rrMerlot.getCurrentPage();
			int pages = total / perPage;

			for( int i = currentPage + 1; i <= Ints.min(pages, MAX_PAGES); i++ )
			{
				rrMerlot = rrMerlot.clickPaging(i);
				assertTrue(rrMerlot.onPage(i));
			}
		}
	}

	// @Test
	public void testMerlotBreadcrumbs()
	{
		ContributePage contribPage = new ContributePage(context).load();
		RemoteRepoMerlotSearchPage rrMerlot = contribPage.openRemoteRepo(MERLOT,
			new RemoteRepoMerlotSearchPage(context));

		String query = "Music Acoustics";
		RemoteRepoListPage searchResults = rrMerlot.search(query);
		assertTrue(searchResults.doesResultExist(query, 1));

		RemoteRepoSearchResult sr = searchResults.getResultForTitle(query, 1);
		RemoteRepoViewMerlotResultPage merlotViewResult = sr.viewResult(new RemoteRepoViewMerlotResultPage(context));

		rrMerlot = merlotViewResult.clickRemoteRepoBreadcrumb(MERLOT);
		assertTrue(rrMerlot.results().doesResultExist(query, 1));

		contribPage = rrMerlot.clickContributeBreadcrumb();
		assertTrue(contribPage.hasRemoteRepo(MERLOT));
	}

	// @Test(dataProvider = "sortOptions")
	public void testMerlotSortOptions(String sortOption)
	{
		SearchPage sp = new SearchPage(context).load();
		RemoteRepoPage rrp = sp.searchOtherRepositories();
		assertTrue(rrp.isRemoteRepositoryVisible(MERLOT));

		RemoteRepoMerlotSearchPage rrMerlot = rrp
			.clickRemoteRepository(MERLOT, new RemoteRepoMerlotSearchPage(context));

		rrMerlot.search("Test");

		rrMerlot.setSort(sortOption);
		assertTrue(rrMerlot.ensureSortSelected(sortOption));

		// TODO Check result order
	}

	// @Test
	public void testMerlotFilterOptions()
	{
		final String QUERY = "Test Search";
		SearchPage sp = new SearchPage(context).load();
		RemoteRepoPage rrp = sp.searchOtherRepositories();
		assertTrue(rrp.isRemoteRepositoryVisible(MERLOT));

		RemoteRepoMerlotSearchPage rrMerlot = rrp
			.clickRemoteRepository(MERLOT, new RemoteRepoMerlotSearchPage(context));

		rrMerlot.exactQuery(QUERY);
		int noFilterResults = rrMerlot.totalItemFound();
		// TODO: Test accuracy of filters (are they filtering correctly), unless
		// a filter encompasses all of a result, the result size should be
		// smaller

		// Community
		rrMerlot.setCommunityFilter("Biology");
		int filterResults = rrMerlot.totalItemFound();
		assertTrue(noFilterResults > filterResults);
		rrMerlot.setCommunityFilter("All");
		// Material Type
		rrMerlot.setMaterialFilter("Quiz/Test");
		filterResults = rrMerlot.totalItemFound();
		assertTrue(noFilterResults > filterResults);
		rrMerlot.setMaterialFilter("All");
		// Category
		rrMerlot.setCategoryFilter("Education");
		filterResults = rrMerlot.totalItemFound();
		assertTrue(noFilterResults > filterResults);
		// Subcategory
		rrMerlot.setSubcategoryFilter("TeacherEd/Teaching Methods/Social Science");
		assertTrue(filterResults > rrMerlot.totalItemFound());
		rrMerlot.setCategoryFilter("All");
		// Keyword (switch noFilterResults to `Any Word` constraint)
		filterResults = rrMerlot.totalItemFound();
		rrMerlot.setKeywordConstraintFilter("Any word");
		rrMerlot.exactQuery(QUERY);
		noFilterResults = rrMerlot.totalItemFound();
		assertTrue(noFilterResults > filterResults);
		// TODO: exact keyword constraints
		// Free / Creative
		rrMerlot.setLicenceFilters(true, false);
		rrMerlot.exactQuery(QUERY);
		filterResults = rrMerlot.totalItemFound();
		assertTrue(noFilterResults > filterResults);
		rrMerlot.setLicenceFilters(false, true);
		rrMerlot.exactQuery(QUERY);
		filterResults = rrMerlot.totalItemFound();
		assertTrue(noFilterResults > filterResults);
		rrMerlot.setLicenceFilters(false, false);
		// Language
		rrMerlot.setLanguageFilter("French");
		rrMerlot.exactQuery(QUERY);
		filterResults = rrMerlot.totalItemFound();
		assertTrue(noFilterResults > filterResults);
		rrMerlot.setLanguageFilter("All");
		// Format
		rrMerlot.setFormatFilter("Audio");
		rrMerlot.exactQuery(QUERY);
		filterResults = rrMerlot.totalItemFound();
		assertTrue(noFilterResults > filterResults);
		rrMerlot.setFormatFilter("All");
		// Audience
		rrMerlot.setAudienceFilter("High School");
		rrMerlot.exactQuery(QUERY);
		filterResults = rrMerlot.totalItemFound();
		assertTrue(noFilterResults > filterResults);
		rrMerlot.setAudienceFilter("All");
		// LMS
		// currently busted as of 10/10/2012
	}

	// @Test
	public void testCreationDateFilter()
	{
		// TODO
	}

}
