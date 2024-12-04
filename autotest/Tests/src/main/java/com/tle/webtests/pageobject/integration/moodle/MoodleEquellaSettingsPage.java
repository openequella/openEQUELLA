package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class MoodleEquellaSettingsPage extends MoodleBasePage<MoodleEquellaSettingsPage> {

  @FindBy(id = "id_s__equella_url")
  private WebElement urlField;

  @FindBy(id = "id_s__equella_action")
  private WebElement actionField;

  @FindBy(id = "id_s__equella_shareid")
  private WebElement idField;

  @FindBy(id = "id_s__equella_sharedsecret")
  private WebElement secretField;

  @FindBy(id = "id_s__equella_editingteacher_shareid")
  private WebElement teacherIdField;

  @FindBy(id = "id_s__equella_editingteacher_sharedsecret")
  private WebElement teacherSecretField;

  @FindBy(id = "id_s__equella_manager_shareid")
  private WebElement managerIdField;

  @FindBy(id = "id_s__equella_manager_sharedsecret")
  private WebElement managerSecretField;

  @FindBy(id = "id_s__equella_admin_username")
  private WebElement adminUser;

  @FindBy(id = "id_s__equella_select_restriction")
  private WebElement restrictions;

  @FindBy(id = "id_s__equella_options")
  private WebElement optionsField;

  @FindBy(id = "id_s__equella_open_in_new_window")
  private WebElement newWindowCheck;

  @FindBy(id = "id_s__equella_intercept_files")
  private WebElement interceptFiles;

  @FindBy(id = "id_s__equella_enable_lti")
  private WebElement enableLtiCheckbox;

  @FindBy(id = "id_s__equella_lti_oauth_key")
  private WebElement ltiClientIdField;

  @FindBy(id = "id_s__equella_lti_oauth_secret")
  private WebElement ltiSecretField;

  @FindBy(className = "form-submit")
  private WebElement saveButton;

  public MoodleEquellaSettingsPage(PageContext context) {
    super(context, By.xpath("//h2[contains(text(), 'EQUELLA Resource')]"));
  }

  public void save() {
    saveButton.click();
  }

  public void setUrl(String url) {
    urlField.clear();
    urlField.sendKeys(url);
  }

  public void setAction(String action) {
    actionField.clear();
    actionField.sendKeys(action);
  }

  public void setAllTokenFields(String id, String secret) {
    setSecret(id, secret);
    setTeacherSecret(id, secret);
    setManagerSecret(id, secret);
  }

  public void setSecret(String id, String secret) {
    idField.clear();
    idField.sendKeys(id);

    secretField.clear();
    secretField.sendKeys(secret);
  }

  public void setTeacherSecret(String id, String secret) {
    teacherIdField.clear();
    teacherIdField.sendKeys(id);

    teacherSecretField.clear();
    teacherSecretField.sendKeys(secret);
  }

  public void setAdminUser(String user) {
    adminUser.clear();
    adminUser.sendKeys(user);
  }

  public void setManagerSecret(String id, String secret) {
    managerIdField.clear();
    managerIdField.sendKeys(id);

    managerSecretField.clear();
    managerSecretField.sendKeys(secret);
  }

  /**
   * @param restriction - none, itemonly, attachmentonly
   */
  public void setRestriction(String restriction) {
    new Select(restrictions).selectByValue(restriction);
  }

  public void setOptions(String options) {
    optionsField.clear();
    optionsField.sendKeys(options);
  }

  public void setNewWindow(boolean newWindow) {
    if (newWindowCheck.isSelected() != newWindow) {
      newWindowCheck.click();
    }
  }

  public void enableLti(boolean enable) {
    if (enableLtiCheckbox.isSelected() != enable) {
      enableLtiCheckbox.click();
      ltiClientIdField.clear();
      ltiSecretField.clear();
    }
  }

  // TODO tests for these two methods

  public void setLtiDetails(String clientId, String secret) {
    enableLti(true);
    ltiClientIdField.clear();
    ltiSecretField.clear();
    ltiClientIdField.sendKeys(clientId);
    ltiSecretField.sendKeys(secret);
  }

  public void setInterceptMode(String option) {
    Select select = new Select(interceptFiles);
    select.selectByValue(option);
  }
}
