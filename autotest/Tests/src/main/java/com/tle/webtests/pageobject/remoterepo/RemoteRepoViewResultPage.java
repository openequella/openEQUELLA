package com.tle.webtests.pageobject.remoterepo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;

public abstract class RemoteRepoViewResultPage<T extends RemoteRepoViewResultPage<T>> extends AbstractPage<T>
{
	@FindBy(xpath = "//button[contains(@id, '_importButton')]")
	private WebElement importButton;

	public RemoteRepoViewResultPage(PageContext context, By loadedBy)
	{
		super(context, loadedBy);
	}

	public WizardPageTab importResult()
	{
		importButton.click();
		return new WizardPageTab(context, 0).get();
	}
}
