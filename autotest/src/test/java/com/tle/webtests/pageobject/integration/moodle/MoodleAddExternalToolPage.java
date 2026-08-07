package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class MoodleAddExternalToolPage extends MoodleBasePage<MoodleAddExternalToolPage> {
  @FindBy(id = "id_name")
  private WebElement activityName;

  @FindBy(id = "id_typeid")
  private WebElement toolType;

  @FindBy(id = "id_toolurl")
  private WebElement launchUrl;

  @FindBy(id = "id_submitbutton2")
  private WebElement save;

  @FindBy(id = "id_cancel")
  private WebElement cancel;

  @FindBy(linkText = "Show more...")
  private WebElement advancedLink;

  @FindBy(id = "id_resourcekey")
  private WebElement consumerKey;

  @FindBy(id = "id_password")
  private WebElement sharedSecret;

  @FindBy(name = "mform_showadvanced")
  private WebElement showAdvancedButton;

  public MoodleAddExternalToolPage(PageContext context) {
    super(context, By.id("id_toolurl"));
  }

  public MoodleCoursePage setupExternalTool(
      String name, String type, String url, String courseName) {
    // Setup link
    activityName.clear();
    activityName.sendKeys(name);
    if (type != null) {
      toolType.findElement(By.xpath("//select/option[text()='" + type + "']")).click();
    }
    launchUrl.clear();
    launchUrl.sendKeys(url);

    scrollIntoViewAndClick(save);

    return new MoodleCoursePage(context, courseName).get();
  }

  public void addKeySecret(String key, String secret) {
    waitForElement(advancedLink);
    advancedLink.click();
    getWaiter().until(ExpectedConditions.visibilityOf(consumerKey));
    consumerKey.sendKeys(key);
    sharedSecret.sendKeys(secret);
  }
}
