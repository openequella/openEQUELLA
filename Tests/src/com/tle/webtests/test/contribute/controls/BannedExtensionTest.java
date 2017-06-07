package com.tle.webtests.test.contribute.controls;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import com.tle.webtests.test.files.Attachments;

@TestInstitution("fiveo")
public class BannedExtensionTest extends AbstractCleanupAutoTest
{

	private static final String FILE_NAME = "banned.exe";
	private static final String COLLECTION = "Navigation and Attachments";

	@Test
	public void uploadBannedFile()
	{
		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
		String fullName = context.getFullName("banned file");
		wizard.editbox(1, fullName);
		UniversalControl control = wizard.universalControl(2);
		FileUniversalControlType fileControl = control.addResource(new FileUniversalControlType(control));
		fileControl.uploadError(Attachments.get(FILE_NAME), FILE_NAME
			+ ": File upload cancelled.  File extension has been banned");
		fileControl.close();
		SummaryPage summary = wizard.save().publish();

		assertFalse(summary.hasAttachmentsSection());
	}
}
