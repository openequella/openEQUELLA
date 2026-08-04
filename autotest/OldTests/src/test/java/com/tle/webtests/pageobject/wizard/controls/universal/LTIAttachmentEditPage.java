/** */
package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.WebElement;

public class LTIAttachmentEditPage extends AbstractAttachmentEditPage<LTIAttachmentEditPage> {
  private LTICommonControlContainerImpl ccc;

  public LTIAttachmentEditPage(UniversalControl universalControl) {
    super(universalControl);
    ccc = new LTICommonControlContainerImpl(control, getWizid());
  }

  /**
   * @see LTICommonControlContainerImpl#enterValues(String, int, String)
   * @param attachmentNewName
   * @param selectionIndex
   * @param launchUrl
   */
  public void enterValues(String attachmentNewName, int selectionIndex, String launchUrl) {
    ccc.enterValues(attachmentNewName, selectionIndex, launchUrl);
  }

  /**
   * @see LTICommonControlContainerImpl#enterAdvancedValues(String, String, String, Boolean,
   *     Boolean, Boolean)
   * @param consumerKey
   * @param sharedSecret
   * @param customParams
   * @param defaultPrivacy
   * @param shareName
   * @param shareEmail
   */
  public void enterAdvancedValues(
      String consumerKey,
      String sharedSecret,
      String customParams,
      Boolean defaultPrivacy,
      Boolean shareName,
      Boolean shareEmail) {
    ccc.enterAdvancedValues(
        consumerKey, sharedSecret, customParams, defaultPrivacy, shareName, shareEmail);
  }

  @Override
  public WebElement getNameField() {
    return ccc.getNameField();
  }

  @Override
  protected WebElement findLoadedElement() {
    return getNameField();
  }
}
