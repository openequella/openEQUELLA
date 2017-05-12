package com.tle.webtests.pageobject.integration.moodle;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.tle.common.Check;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.SummaryPage;

public class MoodleSelectionPage extends AbstractPage<MoodleSelectionPage>
{
	private final MoodleCoursePage coursePage;

	public MoodleSelectionPage(MoodleCoursePage coursePage)
	{
		super(coursePage.getContext(), By.xpath("//h2[contains(text(),'Adding a new EQUELLA Resource')]"));
		this.coursePage = coursePage;
	}

	public MoodleCoursePage addItem(int week, String search)
	{
		return addItem(week, search, null);
	}

	public MoodleCoursePage addItem(int week, String search, String attachment)
	{
		switchToSelection();
		SelectionSession selectionSession = new SelectionSession(context).get();
		SummaryPage summary = selectionSession.homeExactSearch(search).getResult(1).viewSummary();
		if( Check.isEmpty(attachment) )
		{
			return summary.selectMultipleItem(coursePage);
		}
		else
		{
			return summary.attachments().selectAttachmentMultiple(attachment, coursePage);
		}
	}

	public MoodleCoursePage addPackage(int week, String search)
	{
		switchToSelection();
		SelectionSession selectionSession = new SelectionSession(context).get();
		SummaryPage summary = selectionSession.homeExactSearch(search).getResult(1).viewSummary();
		return summary.attachments().selectPackage(coursePage);
	}

	public MoodleCoursePage addItemFromSearchResult(int week, String search)
	{
		switchToSelection();
		SelectionSession selectionSession = new SelectionSession(context).get();
		ItemListPage searchPage = selectionSession.homeExactSearch(search);
		searchPage.getResult(1).setChecked(true, true);
		return selectionSession.finishedSelecting(coursePage);
	}

	public SelectionSession equellaSession()
	{
		switchToSelection();
		return new SelectionSession(context).get();
	}

	public MoodleCoursePage cancel()
	{
		driver.switchTo().defaultContent();
		driver.findElement(By.xpath("//button[contains( @class, 'yui3-button-close')]")).click();
		driver.findElement(By.linkText("Cancel")).click();
		return coursePage.get();
	}

	private void switchToSelection()
	{

		boolean chrome = context.getTestConfig().isChromeDriverSet();
		if( chrome )
		{
			WebElement objectTag = waitForElement(By.id("resourceobject"));
			waiter.until(ChromeHacks.convertObjectToiFrame(context, objectTag));
		}
		else
		{
			waiter.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("resourceobject"));
		}

	}
}
