package com.tle.webtests.pageobject.remoterepo.merlot;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.remoterepo.AbstractRemoteRepoSearchPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoListPage;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoSearchResult;

public class RemoteRepoMerlotSearchPage
	extends
		AbstractRemoteRepoSearchPage<RemoteRepoMerlotSearchPage, RemoteRepoListPage, RemoteRepoSearchResult>
{

	@FindBy(xpath = "id('searchform')/h2[text()='Searching MERLOT']")
	private WebElement mainElem;
	@FindBy(id = "mq_cm")
	private WebElement communityFilter;
	@FindBy(id = "mq_m")
	private WebElement materialFilter;
	@FindBy(id = "mq_c")
	private WebElement categoryFilter;
	@FindBy(id = "mq_s")
	private WebElement subcatFilter;

	@FindBy(id = "mfkt_kc")
	private WebElement keywordConstraint;
	@FindBy(id = "mfo_c")
	private WebElement freeCheckbox;
	@FindBy(id = "mfo_cc")
	private WebElement creativeCommonsCheckBox;
	@FindBy(id = "mfo_l")
	private WebElement languageFilter;
	@FindBy(id = "mfo_t")
	private WebElement formatFilter;
	@FindBy(id = "mfo_a")
	private WebElement audienceFilter;

	@FindBy(id = "searchresults")
	private WebElement resultsAjaxDiv;
	@FindBy(id = "searchresults-available")
	private WebElement resultsFoundDiv;

	WaitingPageObject<RemoteRepoMerlotSearchPage> filterWaiter;

	public RemoteRepoMerlotSearchPage(PageContext context)
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

	private WaitingPageObject<RemoteRepoMerlotSearchPage> getResultsWaiter()
	{
		return ajaxUpdateExpect(resultsAjaxDiv, resultsFoundDiv);
	}

	public void setCommunityFilter(String community)
	{
		filterWaiter = getResultsWaiter();
		new EquellaSelect(context, communityFilter).selectByVisibleText(community);
		filterWaiter.get();
	}

	public void setMaterialFilter(String type)
	{
		filterWaiter = getResultsWaiter();
		new EquellaSelect(context, materialFilter).selectByVisibleText(type);
		filterWaiter.get();
	}

	public void setCategoryFilter(String category)
	{
		filterWaiter = getResultsWaiter();
		new EquellaSelect(context, categoryFilter).selectByVisibleText(category);
		filterWaiter.get();
	}

	public void setSubcategoryFilter(String subcategory)
	{
		filterWaiter = getResultsWaiter();
		new EquellaSelect(context, subcatFilter).selectByVisibleText(subcategory);
		filterWaiter.get();
	}

	public void setKeywordConstraintFilter(String constraint)
	{
		new EquellaSelect(context, keywordConstraint).selectByVisibleText(constraint);
	}

	public void setLicenceFilters(boolean free, boolean creativeCommons)
	{
		if( free != freeCheckbox.isSelected() )
		{
			freeCheckbox.click();
		}
		if( creativeCommons != creativeCommonsCheckBox.isSelected() )
		{
			creativeCommonsCheckBox.click();
		}
	}

	public void setLanguageFilter(String language)
	{
		new EquellaSelect(context, languageFilter).selectByVisibleText(language);
	}

	public void setFormatFilter(String format)
	{
		new EquellaSelect(context, formatFilter).selectByVisibleText(format);
	}

	public void setAudienceFilter(String audience)
	{
		new EquellaSelect(context, audienceFilter).selectByVisibleText(audience);
	}

	public int totalItemFound()
	{
		String searchResults = driver.findElement(By.id("searchresults-stats")).getText();
		String totalFound = searchResults.split("\\s+")[5];
		totalFound = totalFound.replace(",", "");
		return Integer.parseInt(totalFound);
	}

}
