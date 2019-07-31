package com.tle.webtests.test.searching;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class UTF8SearchingTest extends AbstractCleanupAutoTest {
  @DataProvider(name = "itemNames")
  public Object[][] itemNames() {
    return new Object[][] {{"хцч", true}, {"¥¥", false}};
  }

  @Test(dataProvider = "itemNames")
  public void addAndSearch(String text, boolean findIt) {
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
    String itemName = context.getFullName(text);
    wizard.editbox(1, itemName);
    wizard.save().publish();

    SearchPage searchPage = new SearchPage(context).load();
    ItemListPage results = searchPage.search(text);
    if (!findIt) {
      assertFalse(results.isResultsAvailable());
    } else {
      assertTrue(results.doesResultExist(itemName, 1));
    }
  }
}
