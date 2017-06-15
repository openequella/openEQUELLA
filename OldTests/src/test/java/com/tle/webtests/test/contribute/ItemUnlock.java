package com.tle.webtests.test.contribute;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.YouTubeUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("fiveo")
public class ItemUnlock extends AbstractCleanupTest
{
	private static final String RENAMED_NAME = "A Video";
	private static final String DISPLAY_NAME = "Original Displayname";

	@Test
	public void unlockAndResumeItem()
	{
		logon("AutoTest", "automated");

		// contribute an item
		String itemName = context.getFullName("Item unlock and resume");
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Youtube Channel Testing Collection");
		wizard.editbox(1, itemName);
		UniversalControl control = wizard.universalControl(2);
		YouTubeUniversalControlType youtube = control.addDefaultResource(new YouTubeUniversalControlType(control));
		youtube.search("maths", "The Khan Academy").selectVideo(1).setDisplayName(DISPLAY_NAME).save();
		SummaryPage item = wizard.save().publish();
		assertTrue(item.attachments().attachmentExists(DISPLAY_NAME));

		item.adminTab().edit();
		// navigate away without saving, then view the item
		SearchPage.searchAndView(context, itemName);
		assertTrue(item.isItemLocked());
		// unlock the item
		item.adminTab().unlockItem();
		assertTrue(!item.isItemLocked());

		item.adminTab().edit();

		control = wizard.universalControl(2);
		control.editResource(new YouTubeUniversalControlType(control), DISPLAY_NAME).setDisplayName(RENAMED_NAME)
			.save();

		SearchPage.searchAndView(context, itemName);
		assertTrue(item.attachments().attachmentExists(DISPLAY_NAME));

		// resume the item
		item.adminTab().resumeItem();
		item = wizard.saveNoConfirm();
		assertEquals(item.attachments().attachmentCount(), 1);
		assertTrue(item.attachments().attachmentExists(RENAMED_NAME));

	}
}
