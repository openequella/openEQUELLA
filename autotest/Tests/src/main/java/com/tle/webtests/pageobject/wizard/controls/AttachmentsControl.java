package com.tle.webtests.pageobject.wizard.controls;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import java.io.File;
import java.net.URL;
import org.openqa.selenium.By;

public class AttachmentsControl extends AbstractPage<AttachmentsControl> {
  private final int ctrlNum;
  private final AbstractWizardControlPage<?> wizardPage;

  public AttachmentsControl(
      PageContext context, int ctrlNum, AbstractWizardControlPage<?> wizardPage) {
    super(context);
    this.ctrlNum = ctrlNum;
    this.wizardPage = wizardPage;
  }

  @Override
  public void checkLoaded() throws Error {
    // assume loaded
  }

  public AbstractWizardControlPage<?> upload(URL url) {
    return upload(getPathFromUrl(url));
  }

  public AbstractWizardControlPage<?> upload(File file) {
    return upload(file.getAbsolutePath());
  }

  private AbstractWizardControlPage<?> upload(String filename) {
    driver.findElement(By.id(wizardPage.subComponentId(ctrlNum, "fileUpload"))).sendKeys(filename);
    driver.findElement(By.id(wizardPage.subComponentId(ctrlNum, "attachButton"))).click();
    return wizardPage.get();
  }
}
