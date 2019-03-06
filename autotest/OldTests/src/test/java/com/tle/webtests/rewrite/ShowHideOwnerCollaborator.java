// This class will focus on Showing and Hiding Owners and Collaborators
package com.tle.webtests.rewrite;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractSessionTest;

@TestInstitution("dinuk")
public class ShowHideOwnerCollaborator extends AbstractSessionTest
{
	@Test
	public void ShowHideOwnerCollaboratorcofigure()
	{
		logon("AutoTest", "automated");

		/* Item Contribution
		
		ContributePage contributePage = new ContributePage(context).get();
		WizardPageTab wizardPage = contributePage.openWizard("Simple Contribution");
		wizardPage.editbox(1, context.getFullName("EDU"));
		wizardPage.editbox(2, context.getFullName("EDU DESC"));
		wizardPage.save().publish();
		*/

		// Owner Off Collaborators Off
		context.getDriver().get(context.getBaseUrl() + "items/57aa18b9-05fe-4cfe-87c7-dd4159a87f39/1/");
		SummaryPage summaryTabPage = new SummaryPage(context).get();
		Assert.assertFalse(summaryTabPage.isOwnerExisting());
		Assert.assertFalse(summaryTabPage.isCollaboratorsExisting());

		// Owner On Collaborators Off
		context.getDriver().get(context.getBaseUrl() + "items/a7fd0a23-ebfe-411e-9f3a-1b6619056dd9/1/");
		summaryTabPage = new SummaryPage(context).get();
		Assert.assertTrue(summaryTabPage.isOwnerExisting());
		Assert.assertFalse(summaryTabPage.isCollaboratorsExisting());

		// Owner Off Collaborators On
		context.getDriver().get(context.getBaseUrl() + "items/c621eccb-6736-440d-a3a2-da93bd7ff17e/1/");
		summaryTabPage = new SummaryPage(context).get();
		Assert.assertFalse(summaryTabPage.isOwnerExisting());
		Assert.assertTrue(summaryTabPage.isCollaboratorsExisting());

		// Owner On Collaborators On
		context.getDriver().get(context.getBaseUrl() + "items/03ae0cc0-f903-42ff-92d7-aeff0f3a0950/1/");
		summaryTabPage = new SummaryPage(context).get();
		Assert.assertTrue(summaryTabPage.isOwnerExisting());
		Assert.assertTrue(summaryTabPage.isCollaboratorsExisting());
	}
}
