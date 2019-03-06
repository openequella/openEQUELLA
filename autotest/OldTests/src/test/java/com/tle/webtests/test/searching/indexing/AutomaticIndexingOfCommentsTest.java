package com.tle.webtests.test.searching.indexing;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.CommentsSection;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class AutomaticIndexingOfCommentsTest extends AbstractCleanupTest
{

	public AutomaticIndexingOfCommentsTest()
	{
		super("AIOCT");
		setDeleteCredentials("admin", "``````");
	}

	/*
	 * This test looks at whether automatic indexing of comments works by
	 * creating an item commenting on it, and then searching for one of the
	 * words in the comment. If it finds the item; we know it works.
	 */

	@Test
	public void autoIndexingofComments()
	{
		// Login as admin user and contribute an item.
		logon("admin", "``````");
		WizardPageTab wizard = new ContributePage(context).load().openWizard("Basic collection for searching");
		String itemName = context.getFullName("Auto");
		wizard.editbox(1, itemName);
		wizard.save().publish();
		logout();

		// Login as the SimpleModerator user and find the contributed item and
		// add a comment to it.
		logon("SimpleModerator", "``````");
		CommentsSection comments = new SearchPage(context).load().search('"' + itemName + '"')
			.viewFromTitle(itemName).commentsSection();

		// Make the comment something fairly obscure so the search won't
		// interfere with another item.
		comments.addComment("lawyer molloy", 0);
		logout();

		// login as the admin user again just for the hell of it, and search for
		// the comment term.
		// Check that the item is returned by this search
		logon("admin", "``````");
		assertTrue(new SearchPage(context).load().exactQuery("lawyer molloy").doesResultExist(itemName, 1));
	}
}
