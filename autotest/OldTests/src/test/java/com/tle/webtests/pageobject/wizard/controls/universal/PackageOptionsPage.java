package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class PackageOptionsPage extends AbstractAttachmentDialogPage<PackageOptionsPage> {
  @FindBy(xpath = "//input[@value='IMS']")
  private WebElement treatAsPackage;

  @FindBy(xpath = "//input[@value='QTITEST']")
  private WebElement treatAsQTIQuiz;

  @FindBy(xpath = "//input[@value='METS']")
  private WebElement treatAsMETS;

  private FileUniversalControlType fileType;

  public PackageOptionsPage(FileUniversalControlType fileType) {
    super(fileType.getControl());
    this.fileType = fileType;
  }

  @Override
  protected WebElement findLoadedElement() {
    return driver.findElement(By.xpath("//h3[text() = 'Package options']"));
  }

  public PackageAttachmentEditPage treatAsPackage() {
    treatAsPackage.click();
    getNextButton().click();
    return fileType.pkgEditor().get();
  }

  public PackageAttachmentEditPage treatAsQTIQuiz() {
    treatAsQTIQuiz.click();
    getNextButton().click();
    return fileType.pkgEditor().get();
  }

  public PackageAttachmentEditPage treatAsMETS() {
    treatAsMETS.click();
    getNextButton().click();
    return fileType.pkgEditor().get();
  }
}
