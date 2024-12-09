package com.tle.webtests.pageobject.institution;

import com.dytech.common.legacyio.ZipUtils;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ImportTab extends InstitutionTab<ImportTab> implements DbSelectable<ImportTab> {

  private static final String ID_SELECTDB = "isimp_selectDatabase";

  @FindBy(id = "isii_fileUpload")
  private WebElement chooseFile;

  @FindBy(id = "isimp_actionButton")
  private WebElement importButton;

  @FindBy(id = "isimp_url")
  private WebElement institutionUrl;

  @FindBy(id = "isimp_filestore")
  private WebElement filestoreLocation;

  @FindBy(id = ID_SELECTDB)
  private WebElement selectDbButton;

  private WebElement getPasswordElem() {
    return driver.findElement(By.id("isimp_adminPassword"));
  }

  private WebElement getPasswordConfirmElem() {
    return driver.findElement(By.id("isimp_adminConfirm"));
  }

  @FindBy(
      xpath =
          "//div[@class='settingRow' and div[@class='settingLabel']//label[text()='Target"
              + " database']]")
  private WebElement dbSettingRow;

  private final WebDriverWait waiter;

  public ImportTab(PageContext context) {
    super(context, "Import institution", "Import new institution");
    waiter = new WebDriverWait(context.getDriver(), 240);
  }

  @Override
  public WaitingPageObject<ImportTab> getUpdateWaiter() {
    return ExpectWaiter.waiter(ExpectedConditions2.updateOfElement(dbSettingRow), this);
  }

  public StatusPage<ImportTab> importInstitution(
      final String url, String shortName, File file, long timeout) {
    chooseFile.sendKeys(file.getAbsolutePath());
    waiter.until(
        new ExpectedCondition<Boolean>() {
          @Override
          public Boolean apply(WebDriver arg0) {
            institutionUrl.clear();
            institutionUrl.sendKeys(url);
            return true;
          }
        });
    selectFirstDbIfPresent();
    filestoreLocation.clear();
    filestoreLocation.sendKeys(shortName);
    WebElement passwordElem = getPasswordElem();
    WebElement passwordConfirmElem = getPasswordConfirmElem();
    passwordElem.clear();
    passwordConfirmElem.clear();
    String adminPassword = context.getTestConfig().getAdminPassword();
    passwordElem.sendKeys(adminPassword);
    passwordConfirmElem.sendKeys(adminPassword);
    importButton.click();
    acceptConfirmation();
    return new StatusPage<ImportTab>(context, this, timeout).get();
  }

  private ImportTab selectFirstDbIfPresent() {
    if (isPresent(By.id(ID_SELECTDB))) {
      selectDbButton.click();
      SelectDbDialog<ImportTab> selectDbPage = new SelectDbDialog<ImportTab>(this, "isimp").get();
      return selectDbPage.selectRandom();
    }
    return this;
  }

  public StatusPage<ImportTab> importInstitution(final String url, String shortName, Path file) {
    File zipFile = null;
    try {
      zipFile = Files.createTempFile("inst", ".zip").toFile();
      ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile));
      ZipUtils.addDirectoryTree(zipStream, "", file.toFile());
      zipStream.close();
      return importInstitution(url, shortName, zipFile, 240);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } finally {
      if (zipFile != null) {
        zipFile.delete();
      }
    }
  }
}
