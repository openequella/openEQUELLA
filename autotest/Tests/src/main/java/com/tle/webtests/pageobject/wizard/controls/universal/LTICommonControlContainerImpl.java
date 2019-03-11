package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * The LTIAttachment creation and attachment edit duplicate almost all the inner controls, so this
 * class acts as a common inner component for the respective test classes.
 *
 * @author larry
 */
public class LTICommonControlContainerImpl
    extends AbstractWizardControlPage<LTICommonControlContainerImpl> {
  protected WebElement byWizIdXPath(String xpath) {
    return byWizIdIdXPath("", xpath);
  }

  protected WebElement byWizIdIdXPath(String postId, String xpath) {
    return driver.findElement(By.xpath("id(" + quoteXPath(getWizid() + postId) + ")" + xpath));
  }

  protected WebElement byWizId(String postfix) {
    return driver.findElement(By.id(getWizid() + postfix));
  }

  public WebElement byDialogXPath(String xpath) {
    return byWizIdIdXPath("_dialog", xpath);
  }

  protected WebElement getMainDiv() {
    return byDialogXPath("//div[contains(@class, 'ltiHandler')]");
  }

  protected WebElement getNameField() {
    return byWizId("_dialog_lh_displayName");
  }

  protected WebElement getSelectProvider() {
    return byDialogXPath("//select[@id=" + quoteXPath(getWizid() + "_dialog_lh_ltisel") + "]");
  }

  protected WebElement getLaunchUrlEditBox() {
    return byWizId("_dialog_lh_launchUrl");
  }

  protected WebElement getConsumerKeyEditBox() {
    return byWizId("_dialog_lh_consumerKey");
  }

  protected WebElement getSharedSecretEditBox() {
    return byWizId("_dialog_lh_sharedSecret");
  }

  protected WebElement getCustomParamsEditBox() {
    return byWizId("_dialog_lh_customParams");
  }

  protected WebElement getIconUrlEditBox() {
    return byWizId("_dialog_lh_iconUrl");
  }

  protected WebElement getUseDefaultPrivacyCheckBox() {
    return byWizId("_dialog_lh_useDefaultPrivacy");
  }

  protected WebElement getShareNameCheckBox() {
    return byWizId("_dialog_lh_shareName");
  }

  protected WebElement getShareEmailCheckBox() {
    return byWizId("_dialog_lh_shareEmail");
  }

  private final String wizid;

  public LTICommonControlContainerImpl(UniversalControl control, String wizid) {
    super(control.getContext(), By.id("wizard-controls"), 0);
    this.wizid = wizid;
  }

  public void setName(String attachmentNewName) {
    getNameField().clear();
    getNameField().sendKeys(attachmentNewName);
  }

  /**
   * if null, leave unchanged, else wipe and send keys; hence empty string means 'clear'. index
   * values <= 0 means leave unchanged.
   *
   * @param attachmentNewName
   * @param selectionIndex
   * @param launchUrl
   */
  public void enterValues(String attachmentNewName, int selectionIndex, String launchUrl) {
    if (attachmentNewName != null) {
      getNameField().clear();
      getNameField().sendKeys(attachmentNewName);
    }

    if (selectionIndex >= 1) {
      getConfiguredToolSelector().selectByIndex(selectionIndex);
    }

    if (launchUrl != null) {
      getLaunchUrlEditBox().clear();
      getLaunchUrlEditBox().sendKeys(launchUrl);
    }
  }

  /**
   * if null, leave unchanged, else wipe and send keys; hence empty string means 'clear'. Likewise
   * Boolean objects being null means ignore.
   *
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
    if (consumerKey != null) {
      getConsumerKeyEditBox().clear();
      getConsumerKeyEditBox().sendKeys(consumerKey);
    }

    if (sharedSecret != null) {
      getSharedSecretEditBox().clear();
      getSharedSecretEditBox().sendKeys(sharedSecret);
    }

    if (customParams != null) {
      getCustomParamsEditBox().clear();
      getCustomParamsEditBox().sendKeys(customParams);
    }

    if (defaultPrivacy != null) {
      boolean defaultPrivacyBool = defaultPrivacy.booleanValue();
      if (defaultPrivacyBool != getUseDefaultPrivacyCheckBox().isSelected()) {
        getUseDefaultPrivacyCheckBox().click();
      }
    }

    if (shareName != null) {
      boolean shareNameBool = shareName.booleanValue();
      if (shareNameBool != getShareNameCheckBox().isSelected()) {
        getShareNameCheckBox().click();
      }
    }

    if (shareEmail != null) {
      boolean shareEmailBool = shareEmail.booleanValue();
      if (shareEmailBool != getShareEmailCheckBox().isSelected()) {
        getShareEmailCheckBox().click();
      }
    }
  }

  private EquellaSelect getConfiguredToolSelector() {
    return new EquellaSelect(context, getSelectProvider());
  }

  public String getWizid() {
    return wizid;
  }

  @Override
  public String getControlId(int ctrlNum) {
    return "p" + pageNum + "c" + ctrlNum;
  }
}
