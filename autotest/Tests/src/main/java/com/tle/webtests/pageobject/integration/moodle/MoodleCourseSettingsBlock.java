package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MoodleCourseSettingsBlock extends MoodleBasePage<MoodleCourseSettingsBlock> {
  private By settingsDiv;

  @FindBy(className = "backup-files-table")
  private WebElement backupTable;

  public MoodleCourseSettingsBlock(PageContext context) {
    super(context);
    settingsDiv =
        By.xpath(
            "//div[contains(@class, 'block_settings') and ./div[@class='header' and"
                + " .//h2[text()='Administration']]]");

    this.loadedBy = settingsDiv;
  }

  public String backupCourse() {
    clickSettingLink("Backup");
    waitForStage("1. Initial settings");
    clickNext();
    waitForStage("2. Schema settings");
    clickNext();
    waitForStage("3. Confirmation and review");
    String filename = driver.findElement(By.id("id_setting_root_filename")).getAttribute("value");
    clickButton("Perform backup");
    waitForStage("5. Complete");
    clickButton("Continue");
    return filename;
  }

  private void waitForStage(String stage) {
    waitForElement(By.xpath(getStageXpath(stage)));
  }

  private void clickButton(String named) {
    clickButton(driver, named);
  }

  private void clickButton(SearchContext context, String named) {
    context.findElement(By.xpath(".//input[@value=" + quoteXPath(named) + "]")).click();
  }

  private void clickNext() {
    clickButton("Next");
  }

  private String getStageXpath(String string) {
    return "//span[contains(@class, 'backup_stage_current') and text() = "
        + quoteXPath(string)
        + "]";
  }

  public <T extends PageObject> T restoreCourse(String filename, WaitingPageObject<T> page) {
    clickSettingLink("Restore");

    waitForElement(By.name("backupfilechoose"));
    WebElement row =
        backupTable.findElement(By.xpath(".//tr[td[text()=" + quoteXPath(filename) + "]]"));
    row.findElement(By.linkText("Restore")).click();
    waitForStage("1. Confirm");
    clickButton("Continue");
    waitForStage("2. Destination");
    WebElement form =
        driver.findElement(By.xpath("//form[.//h2[text() = 'Restore into this course']]"));
    form.findElement(By.xpath(".//input[@value='0' and @name='target']")).click();
    clickButton(form, "Continue");
    waitForStage("3. Settings");
    clickNext();
    waitForStage("4. Schema");
    clickNext();
    waitForStage("5. Review");
    clickButton("Perform restore");
    waitForStage("7. Complete");
    clickButton("Continue");

    return page.get();
  }

  private void clickSettingLink(String string) {
    driver
        .findElement(settingsDiv)
        .findElement(By.xpath(".//a[text() = " + quoteXPath(string) + "]"))
        .click();
  }
}
