package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.viewitem.LMSExportPage;
import com.tle.webtests.pageobject.viewitem.LMSExportPage.LMSCourseNode;

public class ExternalMoveDialog extends AbstractPage<ExternalMoveDialog>
{
	private final BulkActionDialog dialog;
	private final LMSExportPage export;

	public ExternalMoveDialog(BulkActionDialog dialog)
	{
		super(dialog.getContext(), By.id("bmco_folderTree"));
		this.dialog = dialog;
		this.export = new LMSExportPage(context, "bmco");
	}

	public LMSCourseNode clickCourse(String course)
	{
		return export.clickCourse(course);
	}

	public boolean hasCourse(String course)
	{
		return export.hasCourse(course);
	}

	public ExternalMoveDialog showArchived(boolean on)
	{
		export.showArchived(on);
		return this;
	}

	public <T extends PageObject> boolean execute(WaitingPageObject<T> targetPage)
	{
		return dialog.execute().waitAndFinish(targetPage);
	}
}
