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
public class FileSizeRestrictionTest extends AbstractCleanupAutoTest
{
	private static final String FILE_TOO_BIG = "fireworks.dng";
	private static final String COLLECTION = "Attachment filesize restriction collection";

	@Test
	public void uploadFileOverLimit()
	{
		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
		String fullName = context.getFullName("file that's too big");
		wizard.editbox(1, fullName);
		UniversalControl control = wizard.universalControl(2);
		FileUniversalControlType fileControl = control.addResource(new FileUniversalControlType(control));
		assertEquals(fileControl.uploadError(Attachments.get(FILE_TOO_BIG)).getErrorMessage(),
			"This file cannot be uploaded because it is larger than the maximum file size allowed.");
		fileControl.close();
		SummaryPage summary = wizard.save().publish();

		assertFalse(summary.hasAttachmentsSection());
	}

}
