package com.tle.webtests.test.viewing.actions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.ChangeOwnershipPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("vanilla")
public class ItemSummaryActionsTest extends AbstractCleanupTest
{
	private static final String FIFTY_CHARS = "abcdefghij klmnopqrst uvwxyzabcd efghijklmn opqrst";

	/**
	 * DTEC-14733. The title and description on the item summary page are
	 * limited to 150 characters each for Generic Testing Collection.
	 */
	@Test
	public void enforcedTitleAndDescriptionLength()
	{
		logon("AutoTest", "automated");

		// Start with a title and description less than 150 chars.
		String itemName = context.getFullName(FIFTY_CHARS);
		assertTrue(itemName.length() < 150);

		WizardPageTab wizard = new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
		wizard.editbox(1, itemName);
		wizard.editbox(2, itemName);
		SummaryPage summary = wizard.save().publish();
		assertEquals(summary.getItemTitle(), itemName);
		assertEquals(summary.getItemDescription(), itemName);

		// Now make them longer than 150 chars.
		itemName += FIFTY_CHARS + FIFTY_CHARS;
		assertTrue(itemName.length() > 150);

		wizard = summary.edit();
		wizard.editbox(1, itemName);
		wizard.editbox(2, itemName);
		summary = wizard.saveNoConfirm();

		// Expected name is the first 150 characters and an ellipsis char.
		String expectedName = itemName.substring(0, 150) + "\u2026";
		assertEquals(summary.getItemTitle(), expectedName);
		assertEquals(summary.getItemDescription(), expectedName);

		logout();
	}

	/**
	 * DTEC-14519. Test the changing of ownership and collaborators.
	 */
	@Test
	public void changeOwnership()
	{
		final String itemName = context.getFullName("ownership");
		final String contributor = "AutoTest";
		final String newOwner = "AutoLogin";

		logon(contributor, "automated");

		WizardPageTab wizard = new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
		wizard.editbox(1, itemName);
		ChangeOwnershipPage cop = wizard.save().publish().changeOwnership();

		// Ensure the owner control and summary details say the contributor.
		assertEquals(cop.getOwner(), contributor);
		assertEquals(cop.getSelectedOwner(), contributor);
		assertTrue(cop.getCollaborators().isEmpty());

		cop = cop.changeOwner(newOwner);

		// Ensure the owner control and summary details have updated.
		assertEquals(cop.getOwner(), newOwner);
		assertEquals(cop.getSelectedOwner(), newOwner);
		assertTrue(cop.getCollaborators().isEmpty());

		// Add some collaborators and check things are updated.
		List<String> expectedCollabs = Lists.newArrayList(contributor, "PasswordTest");
		for( String nc : expectedCollabs )
		{
			cop = cop.addCollaborator(nc);
		}
		assertCollaborators(cop, expectedCollabs);

		expectedCollabs.remove(contributor);
		cop.removeCollaborator(contributor);
		assertCollaborators(cop, expectedCollabs);

		// Ensure that other AutoLogin user **can't** change ownership.
		logout();
		logon("AutoLogin", "automated");
		SummaryPage summary = new SearchPage(context).load().exactQuery(itemName).getResultForTitle(itemName, 1)
			.viewSummary();
		assertFalse(summary.canChangeOwnership());
		logout();
	}

	private void assertCollaborators(ChangeOwnershipPage cop, List<String> expected)
	{
		Collections.sort(expected);

		List<String> cs = cop.getCollaborators();
		Collections.sort(cs);
		assertEquals(expected, cs);

		cs = cop.getSelectedCollaborators();
		Collections.sort(cs);
		assertEquals(expected, cs);
	}
}
