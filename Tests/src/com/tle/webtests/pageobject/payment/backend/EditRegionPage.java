package com.tle.webtests.pageobject.payment.backend;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.generic.component.ShuffleBox;
import com.tle.webtests.pageobject.generic.entities.AbstractEditEntityPage;

/**
 * @author Aaron
 */
public class EditRegionPage extends AbstractEditEntityPage<EditRegionPage, ShowRegionsPage>
{
	@FindBy(id = "{editorSectionId}_pl")
	private WebElement predefinedRegions;

	protected EditRegionPage(ShowRegionsPage showRegionsPage)
	{
		super(showRegionsPage);
	}

	@Override
	protected String getEntityName()
	{
		return "region";
	}

	@Override
	public String getEditorSectionId()
	{
		return "re";
	}

	@Override
	public String getContributeSectionId()
	{
		return "rc";
	}

	public void selectCountries(String... countries)
	{
		final ShuffleBox sb = new ShuffleBox(context, "re_cl").get();
		sb.setSelectionByValue(countries);
	}

	public List<String> getSelectedCountries()
	{
		final ShuffleBox sb = new ShuffleBox(context, "re_cl").get();
		return sb.getSelectedValues();
	}

	public void selectPredefinedRegion(String regionName)
	{
		new EquellaSelect(context, predefinedRegions).selectByVisibleText(regionName);
		waitForElement(By.id("re_cl"));
	}
}
