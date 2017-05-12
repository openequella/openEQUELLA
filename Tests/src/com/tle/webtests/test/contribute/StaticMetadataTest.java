package com.tle.webtests.test.contribute;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("contribute")
public class StaticMetadataTest extends AbstractCleanupTest
{
	private static final String COLLECTION = "Static Metadata";
	private static final String TEXT = "This text should appear";
	private static final String TEXT2 = "And so should this";
	private static String USERNAME = "AutoTest";
	private static String PASSWORD = "automated";

	public StaticMetadataTest()
	{
		setDeleteCredentials(USERNAME, PASSWORD);
	}

	@Test
	public void addItem()
	{
		logon(USERNAME, PASSWORD);
		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
		String fullName = context.getFullName("an item");
		wizard.editbox(1, fullName);

		wizard.save().publish();
		assertTrue(isTextPresent(TEXT));
		assertTrue(isTextPresent(TEXT2));
	}
}
