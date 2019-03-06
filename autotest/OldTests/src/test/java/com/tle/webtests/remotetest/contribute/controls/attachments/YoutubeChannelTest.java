package com.tle.webtests.remotetest.contribute.controls.attachments;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.GoogleBooksUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.YouTubeUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("fiveo")
public class YoutubeChannelTest extends AbstractCleanupTest
{
	private static final String MATHS_VIDEO = "Maths video";

	@Test
	public void youTubeChannels()
	{
		logon("AutoTest", "automated");

		String itemName = context.getFullName("YouTube");
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Youtube Channel Testing Collection");
		wizard.editbox(1, itemName);

		// add a video
		UniversalControl control = wizard.universalControl(2);
		YouTubeUniversalControlType youtube = control.addDefaultResource(new YouTubeUniversalControlType(control));
		youtube.search("maths", "The Khan Academy").selectVideo(1, "FIXME");
		control.editResource(youtube.editPage(), "FIXME").setDisplayName(MATHS_VIDEO).save();
		// add another 3 videos
		control.addDefaultResource(youtube).search("test", "ABC News").addVideos(1, 2, 3);

		SummaryPage item = wizard.save().publish();
		assertTrue(item.attachments().attachmentExists(MATHS_VIDEO));
		assertEquals(item.attachments().attachmentCount(), 4);

		// edit a name
		wizard = item.adminTab().edit();
		control = wizard.universalControl(2);
		control.editResource(new YouTubeUniversalControlType(control), MATHS_VIDEO).setDisplayName("A Video").save();
		item = wizard.saveNoConfirm();
		assertTrue(item.attachments().attachmentExists("A Video"));

		// add another video
		item.adminTab().edit();
		control = wizard.universalControl(2);
		YouTubeUniversalControlType addingResorce = control
			.addDefaultResource(new YouTubeUniversalControlType(control));
		addingResorce.search("cat", "All Youtube").selectVideo(2, "FIXME");
		control.editResource(youtube.editPage(), "FIXME").setDisplayName("Cat Video").save();
		item = wizard.saveNoConfirm();
		assertTrue(item.attachments().attachmentExists("Cat Video"));

		// replace a video
		item.adminTab().edit();
		control = wizard.universalControl(2);
		control.replaceSingleResource(new YouTubeUniversalControlType(control), "Cat Video")
			.search("Equella", "All Youtube").selectVideo(2, "FIXME");
		control.editResource(youtube.editPage(), "FIXME").setDisplayName("EQUELLA Video").save();
		item = wizard.saveNoConfirm();
		assertTrue(item.attachments().attachmentExists("EQUELLA Video"));

		// delete videos
		item.adminTab().edit();
		wizard.universalControl(2).deleteResource("EQUELLA Video");
		wizard.universalControl(2).deleteResource("A Video");
		item = wizard.saveNoConfirm();
		assertEquals(item.attachments().attachmentCount(), 3);
	}

	@Test
	public void youTubeNoChannels()
	{
		logon("AutoTest", "automated");
		String itemName = context.getFullName("YouTube");
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Navigation and Attachments");
		wizard.editbox(1, itemName);

		UniversalControl control = wizard.universalControl(2);
		YouTubeUniversalControlType youtube = control.addResource(new YouTubeUniversalControlType(control));
		youtube.search("EQUELLA Intro", null).selectVideo(1, "FIXME");
		control.editResource(youtube.editPage(), "FIXME").setDisplayName("EQUELLA Video").save();
		control.addResource(youtube).search("Test", null).addVideos(1, 2, 3);

		SummaryPage item = wizard.save().publish();
		assertTrue(item.attachments().attachmentExists("EQUELLA Video"));
		assertEquals(item.attachments().attachmentCount(), 4);

		wizard = item.adminTab().edit();

		control = wizard.universalControl(2);
		control.editResource(new YouTubeUniversalControlType(control), "EQUELLA Video").setDisplayName("A Video")
			.save();
		item = wizard.saveNoConfirm();
		assertTrue(item.attachments().attachmentExists("A Video"));

		itemName = context.getFullName("YouTube 3");

		wizard = new ContributePage(context).load().openWizard("Navigation and Attachments");

		control = wizard.universalControl(2);
		youtube = control.addResource(new YouTubeUniversalControlType(control));
		youtube.search("EQUELLA Intro", null).selectVideo(1, "FIXME");
		control.editResource(youtube.editPage(), "FIXME").setDisplayName("EQUELLA Video").save();

		control = wizard.universalControl(2);
		String uuid = control.getAttachmentUuid("EQUELLA Video");
		GoogleBooksUniversalControlType book = control.replaceResource(new GoogleBooksUniversalControlType(control),
			"EQUELLA Video");

		book.search("google").selectBook(1).setDisplayName("A Book").save();

		assertEquals(control.getAttachmentUuid("A Book"), uuid);
		wizard.save().publish();
	}

	/**
	 * http://dev.equella.com/issues/7288
	 */
	@Test
	public void testNoSelections()
	{
		logon("AutoTest", "automated");
		String itemName = context.getFullName("YouTube");
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Navigation and Attachments");
		wizard.editbox(1, itemName);

		UniversalControl control = wizard.universalControl(2);
		YouTubeUniversalControlType youtube = control.addResource(new YouTubeUniversalControlType(control));
		youtube.search("funny cat", null);
		Assert.assertFalse(youtube.canAdd(), "Able to add with no selections");

		UniversalControl close = youtube.close();
		close.getPage().cancel(new ContributePage(context));
	}
}
