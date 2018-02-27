package com.tle.webtests.pageobject.wizard;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.remoterepo.AbstractRemoteRepoSearchPage;

public class ContributePage extends AbstractPage<ContributePage>
{
	@FindBy(className = "contribution-selection-page")
	private WebElement main;

	@FindBy(className = "resumeables")
	private WebElement resumableDiv;

	public ContributePage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return main;
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/contribute.do?old=true");
	}

	public WizardPageTab openWizard(String collection)
	{
		main.findElement(
			By.xpath(".//table[contains(@class, 'zebra')]//a[normalize-space(text())=" + quoteXPath(collection) + "]"))
			.click();
		return new WizardPageTab(context, 0).get();
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractRemoteRepoSearchPage<?, ?, ?>> T openRemoteRepo(String remoteRepo, T type)
	{
		main.findElement(By.linkText(remoteRepo)).click();
		return (T) type.get();
	}

	public boolean hasRemoteRepo(String remoteRepo)
	{
		return isPresent(main, By.linkText(remoteRepo));
	}

	public boolean hasCollection(String collectionName)
	{
		return findCollection(collectionName, "a");
	}

	public boolean hasRemoteRepoOnlyCollection(String collection)
	{
		return findCollection(collection, "span");
	}

	private boolean findCollection(String collection, String tag)
	{
		return isPresent(By.xpath("//table[contains(@class, 'zebra')]/tbody/tr/td/" + tag + "[normalize-space(@title)="
			+ quoteXPath(collection) + "]"));
	}

	public boolean hasResumable(String collection)
	{
		return isPresent(resumableDiv, By.linkText(collection));
	}

	public WizardPageTab openResumable(String collection)
	{
		resumableDiv.findElement(By.linkText(collection)).click();
		return new WizardPageTab(context, 0);
	}

	public void removeResumable(String collection)
	{
		WebElement resume = resumableDiv.findElement(By.linkText(collection));
		WaitingPageObject<ContributePage> removalWaiter = removalWaiter(resume);
		resume.findElement(By.xpath("../..")).findElement(By.linkText("Remove")).click();
		removalWaiter.get();
	}
}
