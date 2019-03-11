package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.WebElement;

public class UrlAttachmentEditPage extends AbstractAttachmentEditPage<UrlAttachmentEditPage> {
  protected WebElement getNameField() {
    return byWizId("_dialog_uh_displayName");
  }

  protected WebElement getPreviewCheckBox() {
    return byWizId("_dialog_uh_previewCheckBox");
  }

  public UrlAttachmentEditPage(UniversalControl universalControl) {
    super(universalControl);
  }

  @Override
  protected WebElement findLoadedElement() {
    return getNameField();
  }

  public UrlAttachmentEditPage setPreview(boolean preview) {
    if (getPreviewCheckBox().isSelected() != preview) {
      getPreviewCheckBox().click();
    }
    return this;
  }

  public boolean canPreview() {
    return isPresent(getPreviewCheckBox());
  }
}
