package com.tle.webtests.test.contribute.controls;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.UrlUniversalControlType;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import com.tle.webtests.test.files.Attachments;
import java.net.URL;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class MaxAttachmentsTest extends AbstractCleanupAutoTest {
  private static final String COLLECTION = "Max attachments restriction";
  private final URL[] ATTACHMENTS = {Attachments.get("page.html"), Attachments.get("pageB.html")};
  private final String LINK_ATTACHMENT_URL = "https://www.google.com";
  private final String LINK_ATTACHMENT_NAME = "Google";

  @Test
  public void maxFiles() {
    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
    String fullName = context.getFullName("maxattachments");
    wizard.editbox(1, fullName);
    UniversalControl control = wizard.universalControl(2);

    UrlUniversalControlType urlControl = control.addResource(new UrlUniversalControlType(control));
    urlControl.addUrl(LINK_ATTACHMENT_URL, LINK_ATTACHMENT_NAME);

    wizard.addFiles(2, false, ATTACHMENTS);
    wizard.save().finishInvalid(wizard);
    Assert.assertEquals(
        wizard.getErrorMessage(2),
        "This control is restricted to a maximum of 2 attachments. Please remove 1 attachment(s).");
    control.deleteResource(LINK_ATTACHMENT_NAME);
    wizard.save().publish();
  }
}
