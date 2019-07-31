package com.tle.webtests.test.contribute.controls.asc;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

@TestInstitution("asc")
public class ScriptedDisplayTemplateTest extends AbstractCleanupTest {
  private static final String ITEM_NAME = "Scripted Template Item ";
  private static final String ITEM_DESC = "This is an item used to test the scripted template";

  // DTEC 14664
  @Test
  public void testSimpleDisplayTemplate() {
    logon("ScriptTest", "automated");
    WebDriver driver = context.getDriver();

    for (int i = 1; i <= 3; i++) {
      contributeItem(ITEM_NAME + i, ITEM_DESC);

      if (i == 1) {
        // Check there are no results
        assertTrue(isPresent(By.id("no-results")));
        WebElement noresults = driver.findElement(By.id("no-results"));
        assertEquals(noresults.getText(), "There are no more items by this contributor");
      } else {
        // Check there are results
        assertTrue(isPresent(By.id("results")));
      }
    }

    // Find and view the first result
    String fullName = context.getFullName(ITEM_NAME);
    SearchPage searchPage = new SearchPage(context).load();
    searchPage.exactQuery(fullName + 1).get();
    searchPage.results().getResult(1).viewSummary().get();

    // assert 2 and 3
    assertRelated(Arrays.asList(fullName + 2, fullName + 3));
    driver.findElement(By.linkText(fullName + 2)).click();
    new SummaryPage(context).get();
    // assert 1 and 3
    assertRelated(Arrays.asList(fullName + 1, fullName + 3));
    driver.findElement(By.linkText(fullName + 3)).click();
    new SummaryPage(context).get();
    // assert 1 and 2
    assertRelated(Arrays.asList(fullName + 1, fullName + 2));
  }

  private SummaryPage contributeItem(String name, String description) {
    ContributePage contribPage = new ContributePage(context).load();
    WizardPageTab wizardPage = contribPage.openWizard("Basic Items").get();
    wizardPage.editbox(1, context.getFullName(name));
    wizardPage.editbox(2, description);
    return wizardPage.save().publish().get();
  }

  private void assertRelated(List<String> items) {
    for (String item : items) {
      assertTrue(isPresent(By.linkText(item)));
    }
  }

  private boolean isPresent(By by) {
    try {
      context.getDriver().findElement(by);
      return true;
    } catch (NotFoundException nfe) {
      return false;
    }
  }
}
