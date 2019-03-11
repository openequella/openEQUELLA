package com.tle.webtests.test.contribute;

import static org.testng.Assert.assertFalse;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardErrorPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractSessionTest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class QuotaExceedTest extends AbstractSessionTest {
  private File twoMegFile;
  private File smallFile;

  @BeforeClass
  public void setUpRandomFiles() throws Exception {
    twoMegFile = createRandomFile(1024 * 1024 * 2);
    smallFile = createRandomFile(2048);
  }

  private File createRandomFile(int bytes) throws IOException {
    File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".blah");
    tempFile.deleteOnExit();
    FileOutputStream out = new FileOutputStream(tempFile);
    Random rand = new Random();
    for (int i = 0; i < bytes; i++) {
      out.write(rand.nextInt());
    }
    out.close();
    return tempFile;
  }

  @SuppressWarnings({"nls", "deprecation"})
  @Test
  public void testQuotaOnAttachment() throws Exception {
    logon("QuotaTest", "``````");
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(GENERIC_TESTING_COLLECTION);
    wizardPage.editbox(1, "Massive file");
    wizardPage.addSingleFile(4, twoMegFile.toURL());
    Assert.assertTrue(
        wizardPage
            .save()
            .publishInvalid(new WizardErrorPage(context))
            .getError()
            .startsWith("Maximum user quota of 1 MB exceeded. "));

    contributePage = new ContributePage(context).load();
    wizardPage = contributePage.openWizard(GENERIC_TESTING_COLLECTION);
    wizardPage.editbox(1, "Small file");
    wizardPage.addSingleFile(4, smallFile.toURL());
    wizardPage.save().publish();
    assertFalse(isTextPresent("Maximum quota of 1 MB exceeded."));
  }
}
