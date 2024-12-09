package com.tle.webtests.test.pss;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.settings.PSSSettingsPage;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;
import org.testng.annotations.Test;

@TestInstitution("pss")
public class PearsonSCORMPlayerTest extends AbstractCleanupTest {
  // Key & Secret from Larry's "SCORM integration" Icodeon consumer account
  private static final String BASE_URL = "http://scormplayer-staging.icodeon.com/";
  private static final String CONSUMER_KEY =
      "9a88edadc6d676293319c3b75810e01a"; // 8ca8f524fc76dda75a1d27da11e88e3f
  private static final String CONSUMER_SECRET =
      "550c6f7063ef4b92bbaf5d8971027b34"; // 18f5b065bea94878ab0881630d21b87b
  private static final String NAMESPACE = "equella";

  private WaitingPageObject<UniversalControl> fixme(UniversalControl control) {
    return control.attachNameWaiter("FROG", false);
  }

  @Test
  public void testLoadScormZip() {
    logon("AutoTest", "automated");
    SettingsPage sp = new SettingsPage(context).load();
    PSSSettingsPage psssp = sp.pssSettingsPage();
    psssp.enablePSS(true);
    // Set fields
    psssp.setBaseUrl(BASE_URL);
    psssp.setConsumerKey(CONSUMER_KEY);
    psssp.setConsumerSecret(CONSUMER_SECRET);
    psssp.setNamespace(NAMESPACE);

    psssp = psssp.save();
    String successReceipt = "Pearson SCORM Services settings saved successfully";
    ReceiptPage.waiter(successReceipt, psssp);

    String itemName = context.getFullName("SCORM package only");
    WizardPageTab wizard = initialItem(itemName);

    // Add a resource control: the 3rd after name & description.
    UniversalControl control = wizard.universalControl(3);
    FileUniversalControlType file =
        control.addDefaultResource(new FileUniversalControlType(control));
    // SCORM package
    file.uploadPackage(Attachments.get(SCORM_ZIP), fixme(control));

    SummaryPage item = wizard.save().publish();

    AttachmentsPage attachments = item.attachments();

    assertTrue(attachments.attachmentExists(SCORM_ZIP));
  }

  private WizardPageTab initialItem(String itemName) {
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Attachments Restricted to SCORM packages");
    wizard.editbox(1, itemName);
    return wizard;
  }

  private static final String SCORM_ZIP = "scorm.zip";

  @Override
  protected void cleanupAfterClass() throws Exception {
    super.cleanupAfterClass();
    SettingsPage sp = new SettingsPage(context).load();
    PSSSettingsPage psssp = sp.pssSettingsPage();
    psssp.enablePSS(false);
    psssp.save();
  }
}
