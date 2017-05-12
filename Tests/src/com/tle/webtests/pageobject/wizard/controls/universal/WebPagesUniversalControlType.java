package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.myresources.AbstractAuthorWebPage;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class WebPagesUniversalControlType extends AbstractUniversalControlType<WebPagesUniversalControlType>
{
	@FindBy(xpath = "id('{wizid}_dialog')//div[contains(@class,'mypagesHandler')]")
	private WebElement mainDiv;
	@FindBy(xpath = "id('{wizid}_dialog')//a[text()='Import pages from scrapbook']")
	private WebElement addScrap;

	@FindBy(id = "{wizid}_dialog_mphmmpe_pageNameField")
	private WebElement nameField;
	@FindBy(id = "{wizid}_dialog_mphmmpp_previewCheckBox")
	private WebElement previewCheckBox;

	public WebPagesUniversalControlType(UniversalControl universalControl)
	{
		super(universalControl);
	}

	@Override
	protected WebElement getPreviewCheckbox()
	{
		return previewCheckBox;
	}

	@Override
	public WebElement getFindElement()
	{
		return mainDiv;
	}

	@Override
	public String getType()
	{
		return "Web pages";
	}

	public UniversalControl addPage(String title, String content)
	{
		return openPage(title, content, false).add(title);
	}

	public WebPagesUniversalControlType openPage(String title, String content, boolean edit)
	{
		AuthorWebPage webPage = new AuthorWebPage(context, page.subComponentId(ctrlnum, "dialog_mphm"), edit).get();
		webPage.addPage(title, content);
		return this;
	}

	public UniversalControl importPageFromScrapbook(String description, String pageAttachment)
	{
		addScrap.click();

		SelectionSession selectionSession = ExpectWaiter.waiter(
			ExpectedConditions.frameToBeAvailableAndSwitchToIt("scrapiframe"), new SelectionSession(context)).get();
		new MyResourcesPage(context, "scrapbook").results().getResultForTitle(description, 1).setChecked(true);
		selectionSession.finishedSelecting(this);
		return add(pageAttachment);
	}

	@Override
	public WebElement getNameField()
	{
		return nameField;
	}

	public UniversalControl add(String newPage)
	{
		addButton.click();
		return control.attachNameWaiter(newPage, false).get();
	}

	public WebPagesUniversalControlType setPreview(boolean b)
	{
		if( previewCheckBox.isSelected() != b )
		{
			previewCheckBox.click();
		}
		return this;
	}

	public static class AuthorWebPage extends AbstractAuthorWebPage<AuthorWebPage>
	{

		public AuthorWebPage(PageContext context, String baseId, boolean edit)
		{
			super(context, baseId, edit);
		}

		@Override
		protected ExpectedCondition<?> getAddCondition()
		{
			return ExpectedConditions2.updateOfElement(addButton);
		}

	}
}
