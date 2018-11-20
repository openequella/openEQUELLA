package com.tle.webtests.pageobject.viewitem;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import com.tle.webtests.pageobject.generic.component.UserSelectedStuff;

public class ChangeOwnershipPage extends ItemPage<ChangeOwnershipPage>
{
	@FindBy(xpath = "//h2[normalize-space(text())='Owner and collaborators']")
	private WebElement titleElem;
	@FindBy(xpath = "id('owner')//a[text()='Change']")
	private WebElement changeOwnerButton;
	@FindBy(id = "collaborators")
	private WebElement collabElem;

	public ChangeOwnershipPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return titleElem;
	}

	public String getSelectedOwner()
	{
		return driver.findElement(By.xpath("id('owner')//td[@class='name']/span")).getAttribute("title");
	}

	public ChangeOwnershipPage changeOwner(String newOwner)
	{
		WaitingPageObject<ChangeOwnershipPage> updater = updateWaiter();
		changeOwnerButton.click();
		SelectUserDialog d = new SelectUserDialog(context, "1").get();
		d.search(newOwner).select(newOwner);
		return d.finish(updater);
	}

	public ChangeOwnershipPage addCollaborator(String newCollab)
	{
		WaitingPageObject<ChangeOwnershipPage> updater = ajaxUpdate(collabElem);
		getCollabSelectedStuff().clickAddAction();
		SelectUserDialog d = new SelectUserDialog(context, "2").get();
		d.search(newCollab).select(newCollab);
		return d.finish(updater);
	}

	public void removeCollaborator(String collab)
	{
		UserSelectedStuff collabSelectedStuff = getCollabSelectedStuff();
		collabSelectedStuff.clickActionWithConfirm(collab, "Remove", Boolean.TRUE,
			collabSelectedStuff.removalWaiter(collab));
	}

	public List<String> getSelectedCollaborators()
	{
		return getCollabSelectedStuff().getSelections();
	}

	protected UserSelectedStuff getCollabSelectedStuff()
	{
		return new UserSelectedStuff(context, By.id("collaborators"));
	}

	public boolean isOwnershipChanged(String owner)
	{
		String trackerStatus = driver.findElement(By.xpath("//div[@id='adjacentuls']/ul[1]/li[1]")).getText();
		return trackerStatus.equals(owner);
	}

	public boolean isCollaboratorChanged(String collaborator)
	{
		String trackerStatus = driver.findElement(By.xpath("//div[@id='adjacentuls']/ul[1]/li[2]")).getText();
		return trackerStatus.equals(collaborator);
	}
}
