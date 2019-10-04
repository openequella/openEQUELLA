package com.tle.webtests.test.viewing;

import static com.tle.webtests.framework.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.viewitem.VersionsPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class VersionShowAllTest extends AbstractCleanupTest {
  @Test
  public void versionTableTest() {
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
    assertEquals(vp.getStatusByVersion(1).toLowerCase(), "archived");
    assertEquals(vp.getStatusByVersion(2).toLowerCase(), "live");
    assertEquals(vp.getStatusByVersion(3).toLowerCase(), "draft");

    assertEquals(vp.getNameByVersion(1), ARCHIVED);
    assertEquals(vp.getNameByVersion(2), LIVE);
    assertEquals(vp.getNameByVersion(3), DRAFT);
  }
}
