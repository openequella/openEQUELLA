package com.tle.webtests.test.contribute;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.IntegrationTesterPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractIntegrationTest;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

@TestInstitution("contribute")
public class ContributeXmlTest extends AbstractIntegrationTest {
  private static String USERNAME = "AutoTest";
  private static String PASSWORD = "automated";
  private static String SHAREDID = "contribute";
  private static String SECRET = "contribute";
  private static String ACTION = "contribute";

  /**
   * Preserved in the exported contributions - tests/contribute/institution.tar.gz being the uuid
   * for the "Basic Items" collection.
   */
  private static String COLLECTIONS_ID_FROM_TEST_INSTITUTION =
      "b28f1ffe-2008-4f5e-d559-83c8acd79316";

  public ContributeXmlTest() {
    setDeleteCredentials(USERNAME, PASSWORD);
  }

  @Test
  public void contributeWithXml() {
    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    String valueOfItemName = "AutotestContribution";
    String valueOfItemDescription = "Prof. Autotest makes a contribution to science with this xml.";
    itp.setItemXml(
        "<xml><item><name>"
            + valueOfItemName
            + "</name><description>"
            + valueOfItemDescription
            + "</description></item></xml>");

    // 3rd parameter for courseId not required here.
    String returnedUrl =
        itp.getSignonUrl(
            ACTION,
            USERNAME,
            "",
            "contributionCollectionIds=" + COLLECTIONS_ID_FROM_TEST_INSTITUTION);

    assertTrue(returnedUrl != null && returnedUrl.length() > 0);

    WizardPageTab wizard = itp.clickPostToUrlButton(new WizardPageTab(context, 0));

    // Control with "Name" is id/name "c1", with "Description" is id/name "c2"
    WebElement webelem = wizard.getControl(1);
    String theNameVal = webelem != null ? webelem.getAttribute("value") : null;
    webelem = wizard.getControl(2);
    String theDescriptionVal = webelem != null ? webelem.getAttribute("value") : null;

    assertTrue(theNameVal != null && theNameVal.equals(valueOfItemName));
    assertTrue(theDescriptionVal != null && theDescriptionVal.equals(valueOfItemDescription));
  }

  @Override
  protected boolean isCleanupItems() {
    return false;
  }
}
