package com.tle.webtests.test.viewing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.IntegrationTesterPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.PackageViewer;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.UrlUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;

@TestInstitution("contribute")
public class ItemSummaryTest extends AbstractCleanupTest
{

	private static String COLLECTION = "Basic Items";
	private static String COLLECTION2 = "Metadata Mapping";
	private static String COLLECTION3 = "Basic Attachments";

	private static String USERNAME = "AutoTest";
	private static String SHAREDID = "contribute";
	private static String SECRET = "contribute";
	private static String ACTION = "selectOrAdd";

	public ItemSummaryTest()
	{
		setDeleteCredentials(AUTOTEST_LOGON, AUTOTEST_PASSWD);
	}

	@Test
	public void aboutLinkTest()
	{
		String summaryItem = context.getFullName("an item");
		String dontFindItem = context.getFullName("do not find");

		logon();
		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION2);

		wizard.editbox(1, dontFindItem);
		wizard.save().publish();

		wizard = new ContributePage(context).load().openWizard(COLLECTION);

		wizard.editbox(1, summaryItem);
		SummaryPage summary = wizard.save().publish();
		assertEquals(summary.getCollection(), COLLECTION);
		SearchPage search = summary.clickCollection();
		assertEquals(search.getSelectedWithin(), COLLECTION);
		assertFalse(search.exactQuery(dontFindItem).isResultsAvailable());
		assertTrue(search.exactQuery(summaryItem).isResultsAvailable());
	}

	@Test
	public void returnFromFullScreen()
	{
		final String attachment = "package.zip";

		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);

		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION2);
		String fullName = context.getFullName("full screen");
		wizard.editbox(1, fullName);
		wizard = wizard.next();
		UniversalControl control = wizard.universalControl(1);
		FileUniversalControlType file = control.addDefaultResource(new FileUniversalControlType(control));
		file.uploadPackageOption(Attachments.get(attachment)).showStructure().save();
		SummaryPage summary = wizard.save().publish();
		PackageViewer pv = summary.attachments().viewFullscreen();
		assertTrue(pv.hasTitle());
		summary = pv.clickTitle();
		assertEquals(summary.attachments().attachmentCount(), 1);

		IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
		itp.getSignonUrl(ACTION, USERNAME, "", "", true);

		SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));

		summary = session.homeExactSearch(fullName).viewFromTitle(fullName);
		pv = summary.attachments().viewFullscreen();
		assertFalse(pv.hasTitle());
	}

	@Test
	public void testReorderAttachments()
	{
		final String urlOne = "http://urlOne.com";
		final String urlTwo = "http://urlTwo.com";
		final String urlThree = "http://urlThree.com";
		List<String> urls = Arrays.asList(urlOne, urlTwo, urlThree);

		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);

		WizardPageTab wiz = new ContributePage(context).load().openWizard(COLLECTION3);
		String itemName = context.getFullName("reorder item");
		wiz.editbox(1, itemName);

		for( String u : urls )
		{
			UniversalControl universal = wiz.universalControl(3);
			UrlUniversalControlType url = universal.addResource(new UrlUniversalControlType(universal));
			universal = url.addUrl(u, u);

		}

		AttachmentsPage attachments = wiz.save().publish().attachments();
		assertEquals(attachments.attachmentOrder(), urls);
		attachments.startReorder();
		attachments.reorder(urlThree, true);
		attachments.reorder(urlThree, true);
		attachments.reorder(urlOne, false);
		attachments.finishReorder();

		logout();
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
		attachments = SearchPage.searchAndView(context, itemName).attachments();

		urls.set(0, urlThree);
		urls.set(2, urlOne);
		assertEquals(attachments.attachmentOrder(), urls);

	}
}
