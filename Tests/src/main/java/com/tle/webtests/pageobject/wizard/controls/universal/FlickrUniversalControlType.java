package com.tle.webtests.pageobject.wizard.controls.universal;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.CheckList;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class FlickrUniversalControlType extends AbstractUniversalControlType<FlickrUniversalControlType>
{
	public static final String CREATIVE_COMMONS_FILTER = "creative-commons-filter";

	private WebElement getSearchField()
	{
		return byDialogXPath("//input[@id='searchform-query']");
	}
	private WebElement getUserField()
	{
		return byDialogXPath("//input[@id='searchform-flickrid']");
	}

	private WebElement getSearchButton()
	{
		return byDialogXPath("//button[@id='searchform-search']");
	}

	private WebElement getFilterOpener()
	{
		return byWizId("_dialog_fhflsra_filter");
	}


	public WebElement getNameField()
	{
		return byWizId("_dialog_fh_displayName");
	}

	private WebElement getMainDiv()
	{
		return byDialogXPath("//div[contains(@class,'flickrHandler')]");
	}

	@FindBy(id = "searchresults")
	private WebElement resultsDiv;

	private WebElement getResetFilterLink()
	{
		return byWizId("_dialog_fhflrf_resetButton");
	}


	public static enum SearchType
	{
		GENERAL, AUTHOR, GENERAL_ALL_TAGS, GENERAL_ANY_TAGS
	}

	public FlickrUniversalControlType(UniversalControl control)
	{
		super(control);
	}

	@Override
	public WebElement getFindElement()
	{
		return getMainDiv();
	}

	@Override
	public String getType()
	{
		return "Flickr";
	}

	public String addPhoto(String search, int index, String displayName, SearchType type)
	{
		WebElement resultPage = performSearchGetOrdinalResult(search, type, index);

		resultPage.click();

		getNextButton().click();
		waitForElement(getNameField());

		if( !Check.isEmpty(displayName) )
		{
			getNameField().clear();
			getNameField().sendKeys(displayName);
		}

		final String filename = getNameField().getAttribute("value");
		getNextButton().click();
		return filename;
	}

	public WebElement performSearchGetOrdinalResult(String search, SearchType type, int index)
	{
		performSearch(search, type);
		WebElement resultPage = driver.findElement(By.id(page.subComponentId(ctrlnum, "dialog_fhflfsr_results_"
			+ (index - 1))));

		return resultPage;
	}

	public ItemListPage performSearch(String search, SearchType type)
	{
		WebElement field;
		boolean clickSearch = true;
		switch( type )
		{
			case GENERAL:
				useTortSelection("text and tags");
				field = getSearchField();
				break;
			case GENERAL_ANY_TAGS:
				useTortSelection("any tags");
				field = getSearchField();
				break;
			case GENERAL_ALL_TAGS:
				useTortSelection("all tags");
				field = getSearchField();
				break;
			case AUTHOR:
				field = getUserField();
				clickSearch = false;
				break;
			default:
				field = getSearchField();
		}
		field.clear();
		field.sendKeys(search);

		WaitingPageObject<ItemListPage> updateWaiter = resultsUpdateWaiter();
		if( clickSearch )
		{
			getSearchButton().click();
		}
		else
		{
			field.sendKeys(Keys.RETURN);
		}

		return updateWaiter.get();
	}

	public ItemListPage useLicencesToSearch(String[] licencesToSearch)
	{
		CheckList checkList = new CheckList(context, CREATIVE_COMMONS_FILTER);
		WaitingPageObject<ItemListPage> updateWaiter = resultsUpdateWaiter();
		for( String lic : licencesToSearch )
		{
			checkList.setSelectionByText(lic);

		}
		return updateWaiter.get();
	}

	public ItemListPage resetFilters()
	{
		WaitingPageObject<ItemListPage> updateWaiter = resultsUpdateWaiter();
		getResetFilterLink().click();
		return updateWaiter.get();
	}

	protected EquellaSelect getTextAndTagsSelector()
	{
		WebElement selectedOption = driver.findElement(By.id("searchform-in"));
		return new EquellaSelect(context, selectedOption);
	}

	protected EquellaSelect getInstitutionSelector()
	{
		openFilters();
		WebElement selectedInstitution = context.getDriver().findElement(
			By.xpath("//select[contains(@id,'_fhflfbfi_fins')]"));
		return new EquellaSelect(context, selectedInstitution);
	}

	public List<Pair<String, String>> getLicenceOptions()
	{
		openFilters();
		CheckList checkList = new CheckList(context, CREATIVE_COMMONS_FILTER);
		return checkList.getSelectionOptions();
	}

	public List<String> getInstitutionNames()
	{
		List<String> institutionNames = new ArrayList<String>();
		EquellaSelect institutionSelector = getInstitutionSelector();
		List<WebElement> selectableLinks = institutionSelector.getSelectableHyperinks();
		for( WebElement selectableLink : selectableLinks )
		{
			String s = selectableLink.getText();
			institutionNames.add(s);
		}
		institutionSelector.clickOn();
		return institutionNames;
	}

	/**
	 * Ensure the text provided is selected in the selector (which will not
	 * necessarily involve a click and new search.
	 * 
	 * @param whichVisibleValue
	 * @return
	 */
	protected ItemListPage useTortSelection(String whichVisibleValue)
	{
		ItemListPage itemListPage = new ItemListPage(context).get();
		EquellaSelect tortSelector = getTextAndTagsSelector();
		if( !(whichVisibleValue.equals(tortSelector.getSelectedText())) )
		{
			WaitingPageObject<ItemListPage> updateWaiter = resultsUpdateWaiter();
			tortSelector.selectByVisibleText(whichVisibleValue);
			return updateWaiter.get();
		}
		return itemListPage;
	}

	private WaitingPageObject<ItemListPage> resultsUpdateWaiter()
	{
		ItemListPage itemListPage = new ItemListPage(context).get();
		WebElement firstChild = resultsDiv.findElement(By.xpath("*[1]"));
		return ExpectWaiter.waiter(
			ExpectedConditions2.updateOfElementLocated(firstChild, driver,
				By.xpath("id('searchresults')[div[@class='itemlist'] or ul[contains(@class,'standard')]]")),
			itemListPage);
	}

	public ItemListPage useInstitution(String whichVisibleValue)
	{
		ItemListPage resultsPage = new ItemListPage(context).get();
		EquellaSelect institutionSelector = getInstitutionSelector();
		if( !(whichVisibleValue.equals(institutionSelector.getSelectedText())) )
		{
			WaitingPageObject<ItemListPage> updateWaiter = resultsUpdateWaiter();
			institutionSelector.selectByVisibleText(whichVisibleValue);
			return updateWaiter.get();
		}
		return resultsPage;
	}

	private void openFilters()
	{
		By filterContent = By.xpath("id('actioncontent')/div[contains(@class, 'filter')]");
		if( !isPresent(filterContent) )
		{
			getFilterOpener().click();
			waiter.until(ExpectedConditions.visibilityOfElementLocated(filterContent));
		}
	}
}
