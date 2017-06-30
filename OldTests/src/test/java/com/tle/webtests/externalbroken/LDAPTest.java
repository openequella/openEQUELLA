package com.tle.webtests.externalbroken;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractSessionTest;

@TestInstitution("ldap")
public class LDAPTest extends AbstractSessionTest
{
	@DataProvider
	public Object[][] institutions()
	{
		return new Object[][]{{"ldap"}/*, {"ldapad"}*/};
	}

	@Test(dataProvider = "institutions")
	public void ldapLogon(String inst)
	{
		PageContext context = newContext(inst);
		logon(context, "andrew.gibb", "Tle160cst");

		SearchPage sp = new SearchPage(context).load();
		sp.setOwnerFilter("andrew.gibb");
		assertTrue(sp.isOwnerSelected("andrew.gibb"));
		logout(context);

		LoginPage lp = new LoginPage(context).load();
		lp = lp.loginWithError("andrew.gibb", "fail");
		assertEquals(lp.getLoginError(), "Sorry, the details you supplied were invalid.");
	}

	// Simple group based permissions
	@Test(dataProvider = "institutions")
	public void ldapPermissions(String inst)
	{
		PageContext context = newContext(inst);
		logon(context, "andrew.gibb", "Tle160cst");
		SearchPage sp = new SearchPage(context).load();
		ItemListPage results = sp.search("cool");
		assertEquals(results.getResult(1).getTitle(), "LDAPTest - Cool");
		logout(context);

		logon(context, "aaron.holland", "Tle160cst");
		sp = new SearchPage(context).load();
		sp.search("cool");
		assertFalse(sp.hasResults());
		results = sp.search("uncool");
		assertEquals(results.getResult(1).getTitle(), "LDAPTest - Uncool");
		logout(context);

		logon(context, "test.user", "Tle160cst");
		sp = new SearchPage(context).load();
		sp.search("cool");
		assertFalse(sp.hasResults());
		sp.search("uncool");
		assertFalse(sp.hasResults());
		results = sp.search("");
		assertEquals(results.getResult(1).getTitle(), "LDAPTest - All");
		logout(context);
	}

	/*
	@Test
	// Nested group based permission
	public void ldapNestedPermissions()
	{
		// TODO use open LDAP
		PageContext context = newContext("ldapad");
		logon(context, "nick.charles", "Tle160cst");
		SearchPage sp = new SearchPage(context).load();
		assertTrue(sp.hasResults());
		assertEquals(sp.search("LDAPTest - Groupception").getResult(1).getTitle(), "LDAPTest - Groupception");
		logout(context);
	}

	@Test
	public void ldapNestedGroupRoles()
	{
		// TODO use open LDAP
		PageContext context = newContext("ldapad");
		logon(context, "nick.charles", "Tle160cst");
		SearchPage sp = new SearchPage(context).load();
		assertTrue(sp.hasResults());
		ItemListPage ilp = sp.search("LDAPTest - Role*");
		assertTrue(ilp.doesResultExist("LDAPTest - Role1"));
		assertTrue(ilp.doesResultExist("LDAPTest - Role2"));
		logout(context);
	}
	*/

	// Select user controls limited by groups
	@Test(dataProvider = "institutions")
	public void testSearchUsers(String inst)
	{
		PageContext context = newContext(inst);
		logon(context, "andrew.gibb", "Tle160cst");
		WizardPageTab openWizard = new MenuSection(context).get().clickContribute("Basic Items");

		// Cool group
		SelectUserDialog userDialog = openWizard.selectUser(3).openDialog();
		assertTrue(userDialog.search("andrew").containsUsername("andrew.gibb"));
		assertTrue(userDialog.searchWithoutMatch("aaron.holland"));
		assertTrue(userDialog.searchWithoutMatch("nicholas.read"));
		openWizard = userDialog.cancel(openWizard);

		// Uncool group
		userDialog = openWizard.selectUser(4).openDialog();
		assertTrue(userDialog.search("aaron").containsUsername("aaron.holland"));
		assertTrue(userDialog.searchWithoutMatch("andrew"));
		openWizard = userDialog.cancel(openWizard);

		// No group
		userDialog = openWizard.selectUser(5).openDialog();
		assertTrue(userDialog.search("test").containsUsername("test.user"));
		assertTrue(userDialog.search("william").containsUsername("william.bowling"));

		logout(context);
	}

	/*
	// @Test(dataProvider = "institutions")
	@Test
	public void testViaAdvancedScripts()
	{
		// TODO: open ldap and roles(?)
		final List<String> charliesGroups = Arrays.asList("Alpha", "Bravo", "kneedeep", "Delta", "ballsdeep",
			"Charlie", "ankledeep", "groupception");
		PageContext context = newContext("ldapad");
		logon(context, "nick.charles", "Tle160cst");
		WizardPageTab openWizard = new MenuSection(context).get().clickContribute("Basic Items");
		openWizard.editbox(1, getClass().getSimpleName() + " testing");
		SummaryPage summary = openWizard.save().publish();
		List<String> values = summary.getValuesByCustomDisplay();
		// search groups for "alpha" group
		assertEquals(values.get(0), "Alpha");
		// members of alpha and it's child groups
		assertEqualsNoOrder(values.get(1).split(", "), new String[]{"[Andrew Gibb [andrew.gibb]",
				"Nicholas Charles [nick.charles]", "Aaron Holland [aaron.holland]]"});
		// first group returned that nick is a member of
		assertTrue(charliesGroups.contains(values.get(2)));
		// is nick a member of this group (always)
		assertTrue(Boolean.valueOf(values.get(3)));
		// what groups is nick a member of
		assertEqualsNoOrder(values.get(4).split(", "), charliesGroups.toArray());
		logout();

	}
	*/

}
