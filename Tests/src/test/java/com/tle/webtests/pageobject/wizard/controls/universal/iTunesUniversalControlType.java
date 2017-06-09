package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class iTunesUniversalControlType extends AbstractUniversalControlType<iTunesUniversalControlType>
{
	@FindBy(xpath = "id('{wizid}_dialog')//div[contains(@class,'iTunesUHandler')]")
	private WebElement mainDiv;
	@FindBy(id = "{wizid}_dialog_ituh_displayName")
	protected WebElement nameField;
	@FindBy(id = "{wizid}_dialog_ituh_treeView")
	private WebElement tree;

	public iTunesUniversalControlType(UniversalControl control)
	{
		super(control);
		waiter = new WebDriverWait(context.getDriver(), 120);
	}

	@Override
	public WebElement getFindElement()
	{
		return mainDiv;
	}

	@Override
	public String getType()
	{
		return "iTunes U";
	}

	public GenericAttachmentEditPage addTrack(String... titles)
	{
		TreeElement node = null;
		for( String title : titles )
		{
			if( node == null )
			{
				node = new TreeElement(tree, title).get();
			}
			else
			{
				node = node.node(title);
			}
			node = node.expand();
		}

		WaitingPageObject<iTunesUniversalControlType> removalWaiter = removalWaiter(node.getLoadedElement());
		node.add();
		removalWaiter.get();
		return edit();
	}

	@Override
	public WebElement getNameField()
	{
		return nameField;
	}

	public class TreeElement extends AbstractPage<TreeElement>
	{
		@FindBy(xpath = "a[contains(@class, 'expandable-hitarea')]")
		private WebElement expand;
		@FindBy(xpath = "a[contains(@class, 'collapsable-hitarea')]")
		private WebElement collapse;

		@FindBy(xpath = ".//button")
		private WebElement addButton;

		public TreeElement(SearchContext searchContext, String title)
		{
			super(iTunesUniversalControlType.this.context, searchContext, By.xpath(".//li[descendant::span[text() = "
				+ quoteXPath(title) + "]]"));
		}

		public void add()
		{
			addButton.click();
		}

		@Override
		public SearchContext getSearchContext()
		{
			return loadedElement;
		}

		public TreeElement expand()
		{
			WaitingPageObject<TreeElement> waiter;
			if( isPresent(loadedElement, By.xpath("ul/li/span[@class='placeholder']")) )
			{
				waiter = removalWaiter(loadedElement.findElement(By.xpath("ul/li/span[@class='placeholder']")));
			}
			else
			{
				waiter = visibilityWaiter(collapse);
			}
			expand.click();
			return waiter.get();
		}

		public TreeElement node(String title)
		{
			return new TreeElement(loadedElement, title).get();
		}
	}
}
