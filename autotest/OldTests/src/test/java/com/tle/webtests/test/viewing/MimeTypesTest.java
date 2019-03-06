package com.tle.webtests.test.viewing;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.URL;
import java.util.Arrays;

import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.settings.MimeEditorPage;
import com.tle.webtests.pageobject.settings.MimeSearchPage;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;

@TestInstitution("vanilla")
public class MimeTypesTest extends AbstractCleanupTest
{
	private static final String ATTACHMENT_NAME = "test.random";
	private int mimeCount = 0;

	@Name("New item")
	private static PrefixedName ITEM_NAME;

	@Override
	protected void prepareBrowserSession()
	{
		logon("TLE_ADMINISTRATOR", "tle010");
	}

	@Test
	public void addRemoveMimeTypeTest()
	{
		MimeSearchPage mimePage = new MimeSearchPage(context).load();
		addMime("Random mime type", "app/something", "something");
		assertTrue(mimePage.search("something").hasResults());
		mimePage.deleteMime(1);
		assertFalse(mimePage.search("something").hasResults());
	}

	@Test
	public void duplicateMimeTypeTest()
	{
		MimeSearchPage mimePage = new MimeSearchPage(context).load();
		addMime("Dupe check 1", "app/test1", "dupe1");
		assertTrue(mimePage.search("dupe1").hasResults());

		mimePage = new MimeSearchPage(context).load();
		mimePage.addMime().setDetails("Dupe check 2", "app/test1", new String[]{"dupe2"}).saveWithError();
		mimePage = new MimeSearchPage(context).load();
		assertFalse(mimePage.search("dupe2").hasResults());
	}

	@Test
	public void editMimeTypeTest()
	{
		MimeSearchPage mimePage = new MimeSearchPage(context).load();
		addMime("Random mime type", "app/random", "random");
		assertTrue(mimePage.search("random").hasResults());

		WizardPageTab wizard = new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);

		wizard.editbox(1, ITEM_NAME);
		wizard.addSingleFile(4, Attachments.get(ATTACHMENT_NAME));

		AttachmentsPage attachments = wizard.save().publish().attachments();
		assertTrue(attachments.attachmentDetails(ATTACHMENT_NAME).contains("Random mime type"));

		mimePage = new MimeSearchPage(context).load();
		mimePage.search("random");
		mimePage.editMime(1).setDetails("Random B type", "app/random2", null).save();
		assertTrue(mimePage.search("random2").hasResults());

		attachments = new ItemAdminPage(context).load().viewItem(ITEM_NAME).attachments();
		assertTrue(attachments.attachmentDetails(ATTACHMENT_NAME).contains("Random B type"));

		mimePage = new MimeSearchPage(context).load();
		mimePage.search("random2");
		mimePage.deleteMime(1);

		attachments = new ItemAdminPage(context).load().viewItem(ITEM_NAME).attachments();
		assertFalse(attachments.attachmentDetails(ATTACHMENT_NAME).contains("Random B type"));
		assertTrue(attachments.attachmentDetails(ATTACHMENT_NAME).contains("application/octet-stream"));

	}

	@Test
	public void changeIconTest()
	{
		MimeSearchPage mimePage = new MimeSearchPage(context).load();
		addMime("IconMime", "app/icontest", "icon");
		mimePage.search("icontest");
		MimeEditorPage mimeEdit = mimePage.editMime(1);
		URL upIcon = Attachments.get("favicon.ico");
		mimePage = mimeEdit.uploadIcon(upIcon).save();
		mimePage.search("icontest");
		mimePage.editMime(1).restoreIcon().save();

		// TODO: actually test that the icons change rather than relying on an
		// exception if they don't
	}

	private MimeSearchPage addMime(String desc, String type, String... ext)
	{
		MimeSearchPage mimePage = new MimeSearchPage(context).load();
		String[] exts = Arrays.copyOf(ext, ext.length + 1);
		exts[ext.length] = "deleteMe" + mimeCount++;
		return mimePage.addMime().setDetails(desc, type, exts).save();
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon("TLE_ADMINISTRATOR", "tle010");
		MimeSearchPage mimePage = new MimeSearchPage(context).load();
		mimePage = new MimeSearchPage(context).load();
		while( mimePage.search("deleteMe").hasResults() )
		{
			mimePage.deleteMime(1);
		}
		super.cleanupAfterClass();
	}
}
