package com.tle.webtests.test.contribute;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractSessionTest;

@TestInstitution("fiveo")
public class ResumableWizardsTest extends AbstractSessionTest
{
	private final String COLLECTION_NAME = "Wizard Config - None";
	private final String ITEM_NAME = "Test resumable";

	// DTEC 14936
	@Test
	public void testResumption()
	{
		// log on
		logon("AutoTest", "automated");

		// load wizard and enter some data
		ContributePage contributePage = new ContributePage(context).load();
		WizardPageTab wizardPage = contributePage.openWizard(COLLECTION_NAME);
		wizardPage.editbox(1, ITEM_NAME);
		wizardPage.next();

		// navigate away using menu
		MenuSection menuSection = new MenuSection(context).get();
		menuSection.clickMenuLink("searching.do", new SearchPage(context));
		contributePage = new ContributePage(context).load();

		// check for resumable open and check data
		assertTrue(contributePage.hasResumable(COLLECTION_NAME));
		wizardPage = contributePage.openResumable(COLLECTION_NAME);
		assertEquals(wizardPage.getControl(1).getAttribute("value"), ITEM_NAME);

		// remove resumable
		contributePage = new ContributePage(context).load();
		contributePage.removeResumable(COLLECTION_NAME);
		assertFalse(contributePage.hasResumable(COLLECTION_NAME));

		// TODO This dtec test case needs extending to include all the different
		// scenarios e.g there is no resumption of wizards started inside
		// selection sessions or from item edits etc
	}
}
