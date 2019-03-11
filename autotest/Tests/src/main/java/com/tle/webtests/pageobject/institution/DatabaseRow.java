package com.tle.webtests.pageobject.institution;

import com.google.common.base.Function;
import com.tle.webtests.framework.Assert;
import com.tle.webtests.framework.EBy;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DatabaseRow extends AbstractPage<DatabaseRow> {

  private static final String STATUS_MIGRATING = "Migrating";
  private static final String REQUIRES_MIGRATION = "Requires migration";
  // private static final String UNINITIALISED = "Uninitialised";
  private static final String STATUS_ONLINE = "Online";
  private static final String STATUS_OFFLINE = "Offline";
  private final WebElement rowElement;
  private final WebElement statusElement;
  private final WebDriverWait longWaiter;

  public DatabaseRow(PageContext context, WebElement rowElement) {
    super(context);
    this.rowElement = rowElement;
    statusElement = rowElement.findElement(By.className("status"));
    longWaiter = new WebDriverWait(context.getDriver(), 60);
  }

  public void initialise() {
    rowElement.findElement(EBy.buttonText("Initialise")).click();
    acceptConfirmation();
  }

  private String getStatus() {
    return statusElement.getText();
  }

  // public boolean isUninitialised()
  // {
  // return UNINITIALISED.equals(getStatus());
  // }
  //
  private boolean isMigrating() {
    return getStatus().startsWith(STATUS_MIGRATING);
  }

  private boolean isChecking() {
    return getStatus().startsWith("Checking");
  }

  public void waitForCheck() {
    waiter.until(
        new Function<WebDriver, Boolean>() {
          @Override
          public Boolean apply(WebDriver driver) {
            return !isChecking();
          }
        });
  }

  public void waitForMigrate() {
    longWaiter.until(
        new Function<WebDriver, Boolean>() {
          @Override
          public Boolean apply(WebDriver driver) {
            return !isMigrating();
          }
        });
    waitForCheck();
  }

  public void migrate() {
    rowElement.findElement(EBy.buttonText("Migrate")).click();
    acceptConfirmation();
  }

  public void setCheckbox(boolean b) {
    WebElement check = rowElement.findElement(By.name("isdt_bulkBoxes"));
    if (check.isSelected() != b) {
      check.click();
    }
  }

  public MigrationProgressDialog progress() {
    WebElement progButton = rowElement.findElement(EBy.buttonText("Progress"));
    getWaiter().until(ExpectedConditions.elementToBeClickable(progButton));
    progButton.click();
    waitForElement(By.id("isdt_progressDialog"));
    return new MigrationProgressDialog(context).get();
  }

  public void assertOffline() {
    assertStatus(STATUS_OFFLINE);
  }

  private void assertStatusStartsWith(String statusCheck) {
    String status = getStatus();
    if (!status.startsWith(statusCheck)) {
      throw new AssertionError(
          "Wrong status expected '" + statusCheck + "' but found '" + status + "'");
    }
  }

  private void assertStatus(String statusCheck) {
    String status = getStatus();
    Assert.assertEquals(
        status,
        statusCheck,
        "Wrong status expected '" + statusCheck + "' but found '" + status + "'");
  }

  public void assertMigrating() {
    assertStatusStartsWith(STATUS_MIGRATING);
  }

  public void assertOnline() {
    assertStatus(STATUS_ONLINE);
  }

  public void assertRequiresMigrating() {
    assertStatus(REQUIRES_MIGRATION);
  }
}
