package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.WebElement;

public class PackageAttachmentEditPage
    extends AbstractFileAttachmentEditPage<PackageAttachmentEditPage> {
  private WebElement getShowStructure() {
    return byWizId("_dialog_fuh_pd_expandButtons_1");
  }

  public PackageAttachmentEditPage(UniversalControl universalControl) {
    super(universalControl);
  }

  @Override
  public String getTypeId() {
    return "pd";
  }

  public PackageAttachmentEditPage showStructure() {
    getShowStructure().click();
    return this;
  }
}
