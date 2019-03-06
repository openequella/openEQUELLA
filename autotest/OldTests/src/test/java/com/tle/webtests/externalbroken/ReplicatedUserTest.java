package com.tle.webtests.externalbroken;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.List;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("replicated")
public class ReplicatedUserTest extends AbstractCleanupTest
{
	@Test
	public void testLogin()
	{
		logon("Vaekdea", "password");
		HomePage homepage = new HomePage(context).load();
		assertTrue(homepage.isTopicTagVisible("Dashboard"));
		logout();

		logon("AutoTest", "automated");
		homepage = new HomePage(context).load();
		assertTrue(homepage.isTopicTagVisible("Dashboard"));
		logout();
	}

	@Test
	public void testSearchUsers()
	{
		logon("AutoTest", "automated");
		WizardPageTab openWizard = new ContributePage(context).load().openWizard("Basic Items");
		SelectUserDialog userDialog = openWizard.selectUser(2).openDialog();
		assertTrue(userDialog.searchWithoutMatch("Vaekdea"));
		assertTrue(userDialog.searchWithoutMatch("AutoTest"));
		openWizard = userDialog.cancel(openWizard);

		userDialog = openWizard.selectUser(3).openDialog();
		assertTrue(userDialog.search("Vaekdea").containsUsername("Vaekdea"));
		assertTrue(userDialog.searchWithoutMatch("AutoTest"));
		openWizard = userDialog.cancel(openWizard);

		userDialog = openWizard.selectUser(4).openDialog();
		assertTrue(userDialog.search("Vaekdea").containsUsername("Vaekdea"));
		assertTrue(userDialog.search("AutoTest").containsUsername("AutoTest"));

		logout();
	}

	@Test
	public void testViaAdvancedScripts()
	{
		logon("Vaekdea", "password");
		WizardPageTab openWizard = new ContributePage(context).load().openWizard("Basic Items");
		openWizard.editbox(1, getClass().getSimpleName() + " testing");
		SummaryPage summary = openWizard.save().publish();
		List<String> values = summary.getValuesByCustomDisplay();
		assertEquals(values.get(0), "mini");
		assertEquals(values.get(1), "monsta");
		assertTrue(Boolean.valueOf(values.get(2)));
		assertFalse(Boolean.valueOf(values.get(3)));
		assertEquals(values.get(4), "monsta");
		assertEquals(values.get(5), "Pbiisckus Cutfm [Vaekdea]");
		assertEquals(values.get(6), "a role");
		assertTrue(Boolean.valueOf(values.get(7)));
		assertEquals(values.get(8), "administrator");
		assertTrue(Boolean.valueOf(values.get(9)));
		assertEquals(values.get(10), "mini");
		assertEquals(values.get(11), "Vaekdea");
		logout();
	}
}
