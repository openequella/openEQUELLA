/** */
package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.common.Check;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.WebElement;

public class LTIUniversalControlType extends AbstractUniversalControlType<LTIUniversalControlType> {

  private final LTICommonControlContainerImpl ccc;

  public LTIUniversalControlType(UniversalControl control) {
    super(control);
    ccc = new LTICommonControlContainerImpl(control, getWizid());
  }

  public UniversalControl addPage(
      int indexToolProvider, String launchUrl, String initialName, String attachmentName) {
    WaitingPageObject<UniversalControl> waiter = control.attachNameWaiter(initialName, false);
    openPage(indexToolProvider, launchUrl);
    add(waiter);
    GenericAttachmentEditPage editingPage = control.editResource(editPage(), initialName);
    ccc.setName(attachmentName);
    return editingPage.save();
  }

  public LTIUniversalControlType openPage(int indexToolProvider, String launchUrl) {
    getConfiguredToolSelector().selectByIndex(indexToolProvider);
    if (!Check.isEmpty(launchUrl)) {
      ccc.getLaunchUrlEditBox().sendKeys(launchUrl);
    }
    return this;
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

  private EquellaSelect getConfiguredToolSelector() {
    return new EquellaSelect(context, ccc.getSelectProvider());
  }

  public UniversalControl add(WaitingPageObject<UniversalControl> returnTo) {
    getAddButton().click();
    return returnTo.get();
  }

  @Override
  public String getType() {
    return "External tool provider (LTI)";
  }

  @Override
  public WebElement getFindElement() {
    return ccc.getMainDiv();
  }

  @Override
  public WebElement getNameField() {
    return ccc.getNameField();
  }
}
