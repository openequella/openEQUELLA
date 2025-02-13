package com.tle.webtests.pageobject.migration;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class UpgradeStepsPage extends AbstractPage<UpgradeStepsPage> {

  public UpgradeStepsPage(PageContext context) {
    super(context, By.id("mp_confirmBackup"));
    waiter = new WebDriverWait(driver, Duration.ofMinutes(4));
  }

  public void upgrade(String password) {
    driver.findElement(By.id("mp_confirmBackup")).click();
    driver.findElement(By.id("mp_startMigrationButton")).click();

    waiter.until(
        new ExpectedCondition<Boolean>() {
          @Override
          public Boolean apply(WebDriver d) {
            try {
              WebElement element = driver.findElement(By.id("mp_finishedButton"));
              return element.isDisplayed();
            } catch (StaleElementReferenceException ste) {
              return Boolean.FALSE;
            }
          }
        });

    driver.findElement(By.id("mp_finishedButton")).click();
  }
}
