package com.tle.webtests.test.contribute.controls.attachments;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.selection.SelectionCheckoutPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.GenericAttachmentEditPage;
import com.tle.webtests.pageobject.wizard.controls.universal.PickAttachmentTypeDialog;
import com.tle.webtests.pageobject.wizard.controls.universal.ResourceUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.UrlAttachmentEditPage;
import com.tle.webtests.pageobject.wizard.controls.universal.UrlUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;

@TestInstitution("fiveo")
public class AttachmentControlTest extends AbstractCleanupTest
{

	private static final String COLLECTION = "Navigation and Attachments";

	@Override
	protected void prepareBrowserSession()
	{
		logon();
	}

	@Test
	public void url()
	{
		String itemName = context.getFullName("URL");
		WizardPageTab wizard = initialItem(itemName);
		wizard.addUrl(2, "http://www.google.com/");
		SummaryPage item = wizard.save().publish();
		assertTrue(item.attachments().attachmentExists("http://www.google.com/"));
		wizard = item.adminTab().edit();

		UniversalControl universalControl = wizard.universalControl(2);
		UrlAttachmentEditPage edit = universalControl.editResource(new UrlUniversalControlType(universalControl),
			"http://www.google.com/");
		edit.setDisplayName("Google").save();

		item = wizard.saveNoConfirm();
		assertTrue(item.attachments().attachmentExists("Google"));
	}

	@Test
	public void selectResource()
	{
		String itemName = context.getFullName("select me");
		WizardPageTab wizard = initialItem(itemName);
		wizard.addFile(2, "page.html");
		String origId = wizard.save().publish().getItemId().getUuid();

		String itemName2 = context.getFullName("selector");
		wizard = initialItem(itemName2);

		UniversalControl universalControl = wizard.universalControl(8);
		ResourceUniversalControlType resourceDialog = universalControl
			.addDefaultResource(new ResourceUniversalControlType(universalControl));
		SelectionSession selectionSession = resourceDialog.getSelectionSession();
		SummaryPage viewFromTitle = selectionSession.homeExactSearch(itemName).viewFromTitle(itemName, 1);
		GenericAttachmentEditPage selectItem = viewFromTitle.selectItem(resourceDialog.editPage());
		selectItem.save();
		wizard.waitForSelectedItem(itemName);

		SummaryPage item = wizard.save().publish();
		assertTrue(item.attachments().attachmentExists(itemName));
		wizard = item.adminTab().edit();
		wizard.universalControl(8).deleteResource(itemName);

		resourceDialog = wizard.universalControl(8).addDefaultResource(resourceDialog);
		selectionSession = resourceDialog.getSelectionSession();

		SelectionCheckoutPage checkout = selectionSession.homeExactSearch(itemName).viewFromTitle(itemName, 1)
			.attachments().selectSingleAttachment("page.html");
		checkout.returnSelection(resourceDialog.editPage()).save();

		GenericAttachmentEditPage editResource = wizard.universalControl(8).editResource(
			new ResourceUniversalControlType(universalControl), "page.html");
		editResource.setDisplayName("A linked page").save();

		item = wizard.saveNoConfirm();

		assertTrue(item.attachments().attachmentExists("A linked page"),
			"Attachment has the wrong display name, should be 'A linked page'");
		assertTrue(item.attachments().viewAttachment("A linked page", new VerifyableAttachment(context)).isVerified());
		assertEquals(item.getItemIdFromAttachment().getUuid(), origId);
	}

	@Test
	public void selectionCreateAndSelect()
	{
		String selectItem = context.getFullName("select me");
		WizardPageTab wizard = initialItem(selectItem);
		String attachment = "page.html";
		wizard.addFile(2, attachment);
		String selectUuid = wizard.save().publish().getItemId().getUuid();

		String creator = context.getFullName("creator");
		wizard = initialItem(creator);

		UniversalControl universalControl = wizard.universalControl(8);
		ResourceUniversalControlType resourceDialog = universalControl
			.addDefaultResource(new ResourceUniversalControlType(universalControl));
		SelectionSession searchResources = resourceDialog.getSelectionSession();
		WizardPageTab innerWizard = searchResources.contributeSingle();
		String created = context.getFullName("created");
		innerWizard.editbox(1, created);

		resourceDialog = universalControl.addDefaultResource(resourceDialog);
		SelectionSession innerSearch = resourceDialog.getSelectionSession();
		innerSearch.homeExactSearch(selectItem).viewFromTitle(selectItem).attachments()
			.selectSingleAttachment(attachment)
			.returnSelection(resourceDialog.getFrameName(), universalControl.attachNameWaiter(attachment, false));

		innerWizard.save().publish().attachments().selectSingleAttachment(attachment)
			.returnSelection(resourceDialog.editPage()).save();

		SummaryPage summary = wizard.save().publish();

		AttachmentsPage attachments = summary.attachments();
		assertTrue(attachments.attachmentExists(attachment));
		assertTrue(attachments.attachmentDetails(attachment).contains("Type: HTML Document"));
		assertTrue(attachments.viewAttachment(attachment, new VerifyableAttachment(context)).isVerified());
		assertTrue(context.getDriver().getCurrentUrl().contains(selectUuid));
	}

	@Test
	public void clickingBack()
	{
		throw new Error("Needs to be re-written because single files aren't edited");
//		String itemName = context.getFullName("a file");
//		WizardPageTab wizard = initialItem(itemName);
//
//		UniversalControl universalControl = wizard.universalControl(2);
//		FileUniversalControlType file = universalControl.addResource(new FileUniversalControlType(universalControl));
//		PickAttachmentTypeDialog controlDialog = file.uploadFile(Attachments.get("page.html")).backToStart();
//
//		file = controlDialog.clickType(new FileUniversalControlType(universalControl));
//		file.uploadFile(Attachments.get("page2.html")).save();
//
//		SummaryPage item = wizard.save().publish();
//		assertFalse(item.attachments().attachmentExists("page.html"));
//		assertTrue(item.attachments().attachmentExists("page2.html"));
	}

	private WizardPageTab initialItem(String itemName)
	{
		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
		wizard.editbox(1, itemName);
		return wizard;
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("AutoTest", "automated");
		ItemAdminPage filterListPage = new ItemAdminPage(context).load();
		ItemListPage filterResults = filterListPage.all().exactQuery("page.html");
		if( filterResults.isResultsAvailable() )
		{
			filterListPage.bulk().deleteAll();
			filterListPage.get().search("page.html");
			filterListPage.bulk().purgeAll();
		}
		super.cleanupAfterClass();
	}

}
