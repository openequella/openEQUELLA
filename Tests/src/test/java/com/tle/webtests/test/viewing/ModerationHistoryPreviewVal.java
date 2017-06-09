package com.tle.webtests.test.viewing;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.PreviewItemDialog;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractSessionTest;


@TestInstitution("dinuk")
public class ModerationHistoryPreviewVal extends AbstractSessionTest{
	
	Integer eventcounter = new Integer(0);
	
	@Test
	public void moderationHistoryPreviewValidation(){
		logon("AutoTest", "automated");
		
		//Access Collection
		ContributePage contributePage = new ContributePage(context).load();
		WizardPageTab wizardTabPage = contributePage.openWizard("MySchema Collection - Preview Check").get();
		
		//Enter Content
		wizardTabPage.editbox(1).setText("Text 1");
		wizardTabPage.editbox(2).setText("Text 2");
		wizardTabPage.editbox(3).setText("Text 3");
		
		//Access Preview Item Screen
		PreviewItemDialog previewItemDialog = wizardTabPage.preview();
		Assert.assertFalse(previewItemDialog.currentItem().hasActions());
	}

}
