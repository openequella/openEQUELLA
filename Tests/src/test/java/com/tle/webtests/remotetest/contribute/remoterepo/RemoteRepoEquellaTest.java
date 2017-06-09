package com.tle.webtests.remotetest.contribute.remoterepo;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoPage;
import com.tle.webtests.pageobject.remoterepo.equella.RemoteRepoEquellaSearchPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.ImageGalleryPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupAutoTest;

@TestInstitution("contribute")
public class RemoteRepoEquellaTest extends AbstractCleanupAutoTest
{
	private static final String REMOTE_ITEM = "SearchSettings - Image 1 - JPEG/JPG";
	private static final String REMOTE_ATTACHMENT = "Cat number one";
	private static final String FIVEO = "fiveo";

	@Test
	public void testEquellaSearchAndContribute()
	{
		// Via search
		SearchPage sp = new SearchPage(context).load();
		RemoteRepoPage rrp = sp.searchOtherRepositories();
		String remoteRepo = FIVEO;

		assertTrue(rrp.isRemoteRepositoryVisible(remoteRepo));

		RemoteRepoEquellaSearchPage rrEquella = rrp.clickRemoteRepository(remoteRepo, new RemoteRepoEquellaSearchPage(
			context));

		rrEquella.downloadAttachments(true);
		SelectionSession browse = rrEquella.browse();
		browse.homeExactSearch(REMOTE_ITEM).viewFromTitle(REMOTE_ITEM).selectItem(rrEquella);
		WizardPageTab wizard = rrEquella.finished().download();
		assertEquals(wizard.editbox(1).getText(), REMOTE_ITEM);
		wizard.editbox(1, context.getFullName("Downloaded Item 1"));
		SummaryPage view = wizard.save().publish();

		assertEquals(view.getItemTitle(), context.getFullName("Downloaded Item 1"));
		AttachmentsPage attachments = view.attachments();
		assertTrue(attachments.attachmentExists(REMOTE_ATTACHMENT));
		String attachmentDetails = attachments.attachmentDetails(REMOTE_ATTACHMENT);
		assertTrue(attachmentDetails.contains("Size: 434.38 KB"), "Attachment should have a size of 434.38 KB. "
			+ attachmentDetails);

		ImageGalleryPage imgPage = attachments.viewAttachment(REMOTE_ATTACHMENT, new ImageGalleryPage(context));
		String src = imgPage.getDisplayedSrc();
		assertTrue(src.contains("cat1.jpg"), "Url was incorrect: " + src);

		sp = new SearchPage(context).load();
		rrp = sp.searchOtherRepositories();
		rrEquella = rrp.clickRemoteRepository(remoteRepo, new RemoteRepoEquellaSearchPage(context));
		rrEquella.downloadAttachments(false);
		browse = rrEquella.browse();
		browse.homeExactSearch(REMOTE_ITEM).viewFromTitle(REMOTE_ITEM).selectItem(rrEquella);
		wizard = rrEquella.finished().download();
		assertEquals(wizard.editbox(1).getText(), REMOTE_ITEM);
		wizard.editbox(1, context.getFullName("Downloaded Item 2"));
		view = wizard.save().publish();

		assertEquals(view.getItemTitle(), context.getFullName("Downloaded Item 2"));
		assertFalse(view.hasAttachmentsSection());
	}
}