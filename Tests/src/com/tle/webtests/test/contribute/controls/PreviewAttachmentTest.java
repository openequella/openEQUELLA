package com.tle.webtests.test.contribute.controls;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileAttachmentEditPage;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.GenericAttachmentEditPage;
import com.tle.webtests.pageobject.wizard.controls.universal.UrlAttachmentEditPage;
import com.tle.webtests.pageobject.wizard.controls.universal.UrlUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.WebPagesUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;

@TestInstitution("ecommerce")
public class PreviewAttachmentTest extends AbstractCleanupTest
{
	private static final String GOOGLE_URL = "http://www.google.com";

	// @Test
	// Disable Preview tests for Web Page attachments - see #7668
	public void previewTest()
	{
		// Contribute as preview, check it's right, disable, check it's now
		// disabled
		// Do this for file/webpage/url types
		// Then check the box is not there if preview is not allowed
		logon("AutoTest", "automated");

		WizardPageTab wizard = new ContributePage(context).load().openWizard("Previewable");
		wizard.editbox(1, context.getFullName(""));
		wizard.addFile(2, Attachments.get("page.html"), true);
		wizard.addUrl(2, GOOGLE_URL, "urlName", true);
		UniversalControl control = wizard.universalControl(2);
		WebPagesUniversalControlType pages = control.addResource(new WebPagesUniversalControlType(control));
		pages.openPage("My web page", "Some basic content", false).setPreview(true).add("My web page");

		UniversalControl universalControl = wizard.universalControl(2);
		// All previews
		assertTrue(universalControl.resourceIsPreview("page.html"));
		assertTrue(universalControl.resourceIsPreview("urlName"));
		assertTrue(universalControl.resourceIsPreview("My web page"));

		SummaryPage summaryTabPage = wizard.save().publish();
		AttachmentsPage attachments = summaryTabPage.attachments();

		assertTrue(attachments.attachmentIsPreview("page.html"));
		assertTrue(attachments.attachmentIsPreview("urlName"));
		assertTrue(attachments.attachmentIsPreview("My web page"));

		wizard = summaryTabPage.edit();
		setFilePreviewStatus("page.html", false, wizard, 2);
		universalControl = wizard.universalControl(2);
		// The file is not a preview
		assertFalse(universalControl.resourceIsPreview("page.html"));
		assertTrue(universalControl.resourceIsPreview("urlName"));
		assertTrue(universalControl.resourceIsPreview("My web page"));

		summaryTabPage = wizard.saveNoConfirm();
		attachments = summaryTabPage.attachments();

		assertFalse(attachments.attachmentIsPreview("page.html"));
		assertTrue(attachments.attachmentIsPreview("urlName"));
		assertTrue(attachments.attachmentIsPreview("My web page"));
		// Just to be sure they're independent

		wizard = summaryTabPage.edit();
		setUrlPreviewStatus("urlName", false, wizard, 2);
		setWebPagePreviewStatus("My web page", false, wizard, 2);
		universalControl = wizard.universalControl(2);

		assertFalse(universalControl.resourceIsPreview("page.html"));
		assertFalse(universalControl.resourceIsPreview("urlName"));
		assertFalse(universalControl.resourceIsPreview("My web page"));

		summaryTabPage = wizard.saveNoConfirm();
		attachments = summaryTabPage.attachments();

		assertFalse(attachments.attachmentIsPreview("page.html"));
		assertFalse(attachments.attachmentIsPreview("urlName"));
		assertFalse(attachments.attachmentIsPreview("My web page"));
	}

	@Test
	public void previewNotAllowedTest()
	{
		logon("AutoTest", "automated");

		ContributePage contributePage = new ContributePage(context);
		WizardPageTab wizard = contributePage.load().openWizard("Non-previewable");
		wizard.editbox(1, context.getFullName("not allowed"));
		UniversalControl control = wizard.universalControl(2);
		FileUniversalControlType files = control.addResource(new FileUniversalControlType(control));
		FileAttachmentEditPage fileEdit = files.uploadFile(Attachments.get("page.html"));
		assertFalse(fileEdit.canPreview());
		fileEdit.save();

		wizard.addUrl(2, GOOGLE_URL, "UrlName");
		UrlAttachmentEditPage urlEditor = control.editResource(new UrlUniversalControlType(control), "UrlName");
		assertFalse(urlEditor.canPreview());
		urlEditor.close();
		wizard.cancel(contributePage);
	}

	// One day I might re-factor to use generics (Not likely)
	private void setFilePreviewStatus(String itemName, boolean preview, WizardPageTab wizard, int ctrlnum)
	{
		UniversalControl control = wizard.universalControl(ctrlnum);
		FileAttachmentEditPage editor = control.editResource(new FileUniversalControlType(control), itemName);
		editor.setPreview(false).save();
	}

	private void setUrlPreviewStatus(String itemName, boolean preview, WizardPageTab wizard, int ctrlnum)
	{
		UniversalControl control = wizard.universalControl(ctrlnum);
		UrlAttachmentEditPage editor = control.editResource(new UrlUniversalControlType(control), itemName);
		editor.setPreview(false).save();
	}

	private void setWebPagePreviewStatus(String itemName, boolean preview, WizardPageTab wizard, int ctrlnum)
	{
		UniversalControl control = wizard.universalControl(ctrlnum);
		GenericAttachmentEditPage editor = control.editResource(new WebPagesUniversalControlType(control), itemName);
		editor.setPreview(false).save();
	}
}
