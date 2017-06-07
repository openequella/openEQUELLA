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
public class IncorrectMimetypeTest extends AbstractCleanupAutoTest
{
	private static final String INCORRECT_FILE_NAME = "charlie.png";
	private static final String COLLECTION = "Attachment mimetype restriction collection";

	@Test
	public void incorrectMimetypeFile()
	{
		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
		String fullName = context.getFullName("incorrect mimetype");
		wizard.editbox(1, fullName);
		UniversalControl control = wizard.universalControl(2);
		FileUniversalControlType fileControl = control.addResource(new FileUniversalControlType(control));
		String errMsg = "This control is restricted to certain file types. \"" + INCORRECT_FILE_NAME
				+ "\" is not allowed to be uploaded.";
		fileControl.uploadError(Attachments.get(INCORRECT_FILE_NAME), errMsg);
		fileControl.close();
		SummaryPage summary = wizard.save().publish();

		assertFalse(summary.hasAttachmentsSection());
	}

}
