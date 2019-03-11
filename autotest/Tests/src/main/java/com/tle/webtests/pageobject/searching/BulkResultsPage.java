package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BulkResultsPage extends AbstractPage<BulkResultsPage> {
  @FindBy(xpath = "//div[@id='bulkresults_container']/p[text() = 'Operations finished']")
  private WebElement finishedMessage;

  public BulkResultsPage(PageContext context) {
    super(context, By.id("bulkresults_container"));
    setMustBeVisible(false);
  }

  public <T extends PageObject> boolean waitAndFinish(WaitingPageObject<T> targetPage) {
    waitForAll();
    boolean noErrors = noErrors();
    close(targetPage);
    return noErrors;
  }

  public BulkResultsPage waitForAll() {
    WebDriverWait waiter = new WebDriverWait(driver, 120);
    waiter.until(ExpectedConditions2.presenceOfElement(finishedMessage));
    return get();
  }

  public boolean noErrors() {
    return getErrors().isEmpty();
  }

  public List<String> getErrors() {
    waitForAll();
    List<WebElement> reasons =
        driver.findElements(
            By.xpath(
                "//table[@id='bss_bulkDialog_bulkResultsTable']//tr[contains(@class, 'failedmsg')]//span[contains(@class, 'reason')]"));
    List<String> reasonTexts = new ArrayList<String>();
    for (WebElement reason : reasons) {
      reasonTexts.add(reason.getText());
    }
    return reasonTexts;
  }

  public boolean errorsContain(String text) {
    List<String> errors = getErrors();
    for (String error : errors) {
      if (error.contains(text)) {
        return true;
      }
    }
    return false;
  }

  public <T extends PageObject> T close(WaitingPageObject<T> targetPage) {
    // TODO: more specific?
    driver.findElement(By.className("modal_close")).click();
    return targetPage.get();
  }
}
