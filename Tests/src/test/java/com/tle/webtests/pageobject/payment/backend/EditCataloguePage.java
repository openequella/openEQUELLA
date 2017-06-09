package com.tle.webtests.pageobject.payment.backend;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.generic.entities.AbstractEditEntityPage;

public class EditCataloguePage extends AbstractEditEntityPage<EditCataloguePage, ShowCataloguesPage>
{
	public static final String EDIT_CATALOUE_PAGE_TITLE = "Edit catalogue";

	@FindBy(id = "{editorSectionId}_rr")
	private WebElement restrictRegionsCheckbox;
	@FindBy(id = "{editorSectionId}_rl")
	private WebElement regionList;
	@FindBy(id = "{editorSectionId}_dc")
	private WebElement dynamicCollection;
	@FindBy(id = "{editorSectionId}_e")
	private WebElement enabledCheckbox;
	@FindBy(id = "regions")
	private WebElement regionsAjaxDiv;

	public EditCataloguePage(ShowCataloguesPage listPage)
	{
		super(listPage);
	}

	public EditCataloguePage setRestrictToRegions(boolean restrict)
	{
		if( restrictRegionsCheckbox.isSelected() != restrict )
		{
			WaitingPageObject<EditCataloguePage> wait;

			if( restrict )
			{
				wait = ajaxUpdateExpect(regionsAjaxDiv, regionList);
			}
			else
			{
				wait = ajaxUpdateEmpty(regionsAjaxDiv);
			}
			restrictRegionsCheckbox.click();
			return wait.get();
		}
		return this;
	}

	public EditCataloguePage clearRegions()
	{
		for( WebElement cb : regionList.findElements(By.xpath("./li/input")) )
		{
			if( cb.isSelected() )
			{
				cb.click();
			}
		}
		return this;
	}

	public EditCataloguePage setRegions(boolean select, PrefixedName... regions)
	{
		for( PrefixedName region : regions )
		{
			WebElement regionCheckbox = regionList.findElement(By.xpath("./li[label/text()="
				+ quoteXPath(region.toString()) + "]/input"));
			if( regionCheckbox.isSelected() != select )
			{
				regionCheckbox.click();
			}
		}
		return this;
	}

	public EditCataloguePage setDynamicCollection(String dynamicCollectionName)
	{
		EquellaSelect dynamicCollectionSelect = new EquellaSelect(context, dynamicCollection);
		dynamicCollectionSelect.selectByVisibleText(dynamicCollectionName);
		return this;
	}

	public EditCataloguePage setEnabled(boolean enabled)
	{
		if( enabledCheckbox.isSelected() != enabled )
		{
			enabledCheckbox.click();
		}
		return this;
	}

	@Override
	protected String getEntityName()
	{
		return "catalogue";
	}

	@Override
	protected String getContributeSectionId()
	{
		return "cc";
	}

	@Override
	protected String getEditorSectionId()
	{
		return "ce";
	}
}
