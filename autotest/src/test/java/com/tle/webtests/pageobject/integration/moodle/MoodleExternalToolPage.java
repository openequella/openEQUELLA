package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class MoodleExternalToolPage extends AbstractPage<MoodleExternalToolPage> {

  @FindBy(linkText = "Add external tool configuration")
  private WebElement addExternalToolType;

  // Browse direct using the URL
  public MoodleExternalToolPage(PageContext context) {
    super(
        context,
        By.xpath(
            "//h2[contains(text(), 'Manage external tool types') or contains(text(), 'External"
                + " Tool') or contains(text(), 'LTI')]"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getIntegUrl() + "admin/settings.php?section=modsettinglti");
  }

  public MoodleExternalToolPage addExternalTool(
      String name, String url, String key, String secret, boolean show) {
    addExternalToolType.click();
    MoodleExternalToolConfigPage extToolConfig = new MoodleExternalToolConfigPage(context).get();
    return extToolConfig.createExternalTool(name, url, key, secret, show).get();
  }

  public boolean hasExternalTool(String name) {
    return isPresent(By.xpath(getToolXpath(name)));
  }

  public MoodleExternalToolPage deleteExternalTool(String name) {
    getToolRow(name).findElement(By.xpath("//td/div/a[@class='editing_delete']")).click();
    return get();
  }

  public MoodleExternalToolConfigPage editExternalTool(String name) {
    getToolRow(name).findElement(By.xpath("//td/div/a[@class='editing_update']")).click();
    return new MoodleExternalToolConfigPage(context).get();
  }

  private String getToolXpath(String name) {
    waitForTable();
    if (isPresent(By.xpath("//div[@id='lti_configured_tools_container']"))) {
      return "//div[@id='lti_configured_tools_container']/table/tbody[@class='yui-dt-data']/tr/td/div[contains(text(),"
                 + " '"
          + name
          + "')]";
    }
    return "//div[@id='lti_configured_container']/table/tbody[@class='yui-dt-data']/tr/td/div[contains(text(),"
               + " '"
        + name
        + "')]";
  }

  private WebElement getToolRow(String name) {
    return driver.findElement(By.xpath(getToolXpath(name) + "/../.."));
  }

  private void waitForTable() {
    // if table -> wait for first row to render properly
    if (isPresent(By.xpath("//div[@id='lti_configured_tools_container']/table"))) {
      getWaiter()
          .until(
              ExpectedConditions.presenceOfElementLocated(
                  By.xpath(
                      "//div[@id='lti_configured_tools_container']/table/tbody[@class='yui-dt-data'][1]")));
    } else if (isPresent(By.xpath("//div[@id='lti_configured_container']/table"))) {
      getWaiter()
          .until(
              ExpectedConditions.presenceOfElementLocated(
                  By.xpath(
                      "//div[@id='lti_configured_container']/table/tbody[@class='yui-dt-data'][1]")));
    }
  }
}
