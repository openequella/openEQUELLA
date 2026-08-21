package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.WebElement;

public abstract class GenericAttachmentEditPage
    extends AbstractAttachmentEditPage<GenericAttachmentEditPage> {

  protected GenericAttachmentEditPage(UniversalControl universalControl) {
    super(universalControl);
  }

  protected abstract WebElement getNameField();

  protected abstract WebElement getPreviewCheckbox();

  @Override
  protected WebElement findLoadedElement() {
    return getNameField();
  }

  public GenericAttachmentEditPage setPreview(boolean preview) {
    WebElement previewCheckbox = getPreviewCheckbox();
    if (previewCheckbox.isSelected() != preview) {
      previewCheckbox.click();
    }
    return this;
  }

  public boolean canPreview() {
    return isPresent(getPreviewCheckbox());
  }
}
