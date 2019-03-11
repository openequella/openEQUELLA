package com.tle.webtests.test.searching;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.BrowseByPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class BrowsebyTest extends AbstractCleanupAutoTest {

  @DataProvider(name = "years", parallel = false)
  public Object[][] years() {
    return new Object[][] {{"1992"}, {"1996"}, {"2010"}, {"2050"}};
  }

  @Override
  protected boolean isCleanupItems() {
    return false;
  }

  @SuppressWarnings("nls")
  @Test(dataProvider = "years")
  public void testBrowseby(String year) throws Exception {
    WizardPageTab wizard = new ContributePage(context).load().openWizard("Browse By Collection");
    String itemName = context.getFullName("Item " + year);
    wizard.editbox(1, itemName);
    wizard.editbox(3, year);
    wizard.save().publish();

    BrowseByPage browseByPage =
        new BrowseByPage(context, "Year", new String[] {"/item/year"}).load();
    assertTrue(browseByPage.isCategoryPresent(year));
  }
}
