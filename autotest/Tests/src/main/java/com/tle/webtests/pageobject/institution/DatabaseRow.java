package com.tle.webtests.pageobject.institution;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.or;
import static com.codeborne.selenide.Selectors.byClassName;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.SelenideElement;
import com.google.common.base.Function;
import com.tle.webtests.framework.Assert;
import com.tle.webtests.framework.EBy;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class DatabaseRow extends AbstractPage<DatabaseRow> {

  private static final String STATUS_MIGRATING = "Migrating";
  private static final String REQUIRES_MIGRATION = "Requires migration";
  // private static final String UNINITIALISED = "Uninitialised";
  private static final String STATUS_ONLINE = "Online";
  private static final String STATUS_OFFLINE = "Offline";
  private static final String TABLE_ID = "isdt_table";
  private static final Duration MIGRATE_TIMEOUT = Duration.ofMinutes(1);
  private final WebElement rowElement;
  // The row's locator, used to re-find the row (and its cells/buttons) fresh via Selenide so
  // reads survive the Databases table's auto-refresh during migration.
  private final By rowBy;

  public DatabaseRow(PageContext context, WebElement rowElement, By rowBy) {
    super(context);
    this.rowElement = rowElement;
    this.rowBy = rowBy;
  }

  public void initialise() {
    // Selenide click waits for the button to be clickable and retries on staleness.
    $(By.id(TABLE_ID)).$(rowBy).$(EBy.buttonText("Initialise")).click();
    acceptConfirmation();
  }

  /** The status cell, re-located fresh via Selenide on each access (no stale cache). */
  private SelenideElement statusCell() {
    return $(By.id(TABLE_ID)).$(rowBy).$(byClassName("status"));
  }

  private String getStatus() {
    return statusCell().getText();
  }

  private boolean isChecking() {
    return getStatus().startsWith("Checking");
  }

  public void waitForCheck() {
    waiter.until((Function<WebDriver, Boolean>) driver -> !isChecking());
  }

  public void waitForMigrate() {
    // Wait for a settled, terminal state (Online or Offline) rather than merely the absence of
    // "Migrating"/"Checking": the latter can be satisfied by a transient pre-migration status
    // (e.g. "Requires migration" before the server has started), causing a premature return.
    statusCell()
        .shouldBe(
            or("migration finished", exactText(STATUS_ONLINE), exactText(STATUS_OFFLINE)),
            MIGRATE_TIMEOUT);
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
