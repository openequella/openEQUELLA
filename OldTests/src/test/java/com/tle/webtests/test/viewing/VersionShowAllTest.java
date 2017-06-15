package com.tle.webtests.test.viewing;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.viewitem.VersionsPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("workflow")
public class VersionShowAllTest extends AbstractCleanupTest
{
	@Test
	public void versionTableTest()
	{
		final String ARCHIVED = context.getFullName("Archived");
		final String LIVE = context.getFullName("Live");
		final String DRAFT = context.getFullName("Draft");

		logon("TLE_ADMINISTRATOR", "tle010");
		WizardPageTab wizard = new ContributePage(context).load().openWizard("No Workflow");
		wizard.editbox(1, ARCHIVED);
		wizard = wizard.save().publish().newVersion();
		wizard.editbox(1, LIVE);
		SummaryPage summary = wizard.save().publish();
		wizard = summary.newVersion();
		wizard.editbox(1, DRAFT);
		summary = wizard.save().draft();
		VersionsPage vp = summary.clickShowAllVersion();
		assertTrue(vp.getStatusByVersion(1).equalsIgnoreCase("archived"));
		assertTrue(vp.getStatusByVersion(2).equalsIgnoreCase("live"));
		assertTrue(vp.getStatusByVersion(3).equalsIgnoreCase("draft"));
		assertTrue(vp.getNameByVersion(1).equalsIgnoreCase(ARCHIVED));
		assertTrue(vp.getNameByVersion(2).equalsIgnoreCase(LIVE));
		assertTrue(vp.getNameByVersion(3).equalsIgnoreCase(DRAFT));

	}
}
