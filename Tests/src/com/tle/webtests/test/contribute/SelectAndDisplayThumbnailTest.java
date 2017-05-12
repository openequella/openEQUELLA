package com.tle.webtests.test.contribute;


import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.SelectThumbnailDialog;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;

@TestInstitution("fiveo")
public class SelectAndDisplayThumbnailTest extends AbstractCleanupTest
{
	private static final String COLLECTION = "Navigation and Attachments";
	private static final String ITEM_NAME_1 = "Thumbnail selection 1";
	private static final String ITEM_NAME_2 = "Thumbnail selection 2";
	private static final String ITEM_NAME_3 = "Thumbnail selection 3";

	@Override
	protected void prepareBrowserSession()
	{
		logon();
	}

	@Test
	public void thumbnailSelectTest()
	{
		WizardPageTab wizard = initialItem(ITEM_NAME_1);

		UniversalControl universalControl = wizard.universalControl(2);
		FileUniversalControlType file = universalControl.addResource(new FileUniversalControlType(universalControl));
		file.uploadFile(Attachments.get("SelectionSessionTest - google.png")).save();

		universalControl = wizard.universalControl(2);
		file = universalControl.addResource(new FileUniversalControlType(universalControl));
		file.uploadFile(Attachments.get("SelectionSessionTest - google2.png")).save();

		SelectThumbnailDialog dialog = wizard.openSelectThumbnailDialog();
		dialog.selectDefault();
		wizard = dialog.saveDialog(wizard);
		SummaryPage view = wizard.save().publish();

		wizard = view.cloneAction().execute();
		wizard.editbox(1, ITEM_NAME_2);
		dialog = wizard.openSelectThumbnailDialog();
		dialog.selectNone();
		wizard = dialog.saveDialog(wizard);
		view = wizard.save().publish();

		wizard = view.cloneAction().execute();
		wizard.editbox(1, ITEM_NAME_3);
		dialog = wizard.openSelectThumbnailDialog();
		dialog.selectCustomThumbnail("SelectionSessionTest - google2.png");
		wizard = dialog.saveDialog(wizard);
		wizard.save().publish();
	}

	// @Test(dependsOnMethods = {"thumbnailSelectTest"})
	public void thumbnailDisplayTest()
	{
		// TODO
		// SearchPage search = new SearchPage(context);
		// ItemListPage results = search.search("ITEM_NAME_1");
	}

	private WizardPageTab initialItem(String itemName)
	{
		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
		wizard.editbox(1, itemName);
		return wizard;
	}


}
