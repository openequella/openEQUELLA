package com.tle.webtests.test.contribute.controls.asc;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import java.util.List;
import org.testng.annotations.Test;

@TestInstitution("asc")
public class PropBagExTest extends AbstractCleanupTest {
  @Test
  public void xmlObjectMethodTests() {
    logon("AutoTest", "automated");

    String itemName = context.getFullName("test item 1");
    WizardPageTab wizard = new ContributePage(context).load().openWizard("PropBagExTestCollection");
    wizard.editbox(1, itemName);

    SummaryPage summary = wizard.save().publish();
    List<String> values = summary.getValuesByCustomDisplay();

    assertTrue(Boolean.valueOf(values.get(0)));
    assertEquals(values.get(1), "value1");
    assertEquals(values.get(2), "value2");
    assertEquals(values.get(3), "value2");
    assertEquals(values.get(4), "1");
    assertTrue(Boolean.valueOf(values.get(5)));
    assertEquals(values.get(6), "value2");
    assertTrue(Boolean.valueOf(values.get(7)));
    assertTrue(Boolean.valueOf(values.get(8)));
    assertTrue(Boolean.valueOf(values.get(9)));
  }
}
