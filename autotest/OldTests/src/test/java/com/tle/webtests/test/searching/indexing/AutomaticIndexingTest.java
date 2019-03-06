package com.tle.webtests.test.searching.indexing;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;

@TestInstitution("workflow")
public class AutomaticIndexingTest extends AbstractCleanupTest
{
	public AutomaticIndexingTest()
	{
		setDeleteCredentials("admin", "``````");
	}

	@DataProvider(name = "filesAndQueries", parallel = false)
	public Object[][] getData()
	{
		return new Object[][]{{"DOC.doc", "msword", 1}, {"html test.html", "myhtmlTest", 2},
				{"text test.txt", "unique", 3}, {"xl2007.xlsx", "ridiculousness", 4}, {"XLS.xls", "msexcel", 5}};
	}

	/* This test is to ensure that automatic indexing of attachments works, by entering text inside a file attachment
	 * and making sure that the item is returned when that term is searched for.
	 */

	@Test(dataProvider = "filesAndQueries")
	public void autoIndexing(String filename, String query, int itemUpto)
	{

		logon("admin", "``````");

		// Contribute a host of different items, attaching the Auto Index files
		// from Domain.
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Basic collection for searching");
		String itemName = context.getFullName("Item" + itemUpto);

		// Upload the file
		wizard.editbox(1, itemName);
		wizard.addSingleFile(4, Attachments.get("indexing/" + filename));
		wizard.save().publish();

		// Now login as another user just for the sake of it, and search for the
		// terms inside the files.
		// If the items are returned, this shows us that the automatic indexing
		// of these file types works.
		logon("SimpleModerator", "``````");
		assertTrue(new SearchPage(context).load().search(query).doesResultExist(itemName, 1));
		logout();
	}
}
