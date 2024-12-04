package com.tle.webtests.pageobject.integration.canvas.course.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.integration.canvas.course.AbstractCanvasCoursePage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CanvasAppsTab extends AbstractCanvasCoursePage<CanvasAppsTab> {
  @FindBy(partialLinkText = "Add App")
  private WebElement addNewAppButton;

  public CanvasAppsTab(PageContext context) {
    super(context, By.id("external_tools"));
  }

  public CanvasAddNewAppDialog addNewApp() {
    addNewAppButton.click();
    return new CanvasAddNewAppDialog(context).get();
  }

  public boolean appExists(String externalToolName) {
    // ugh need a better way to wait for the external tool table ajax, this
    // won't work if a tool already exists
    waiter.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("external_tool")));
    for (WebElement td : driver.findElements(By.className("external_tool"))) {
      if (td.getText().equals(externalToolName)) {
        return true;
      }
    }
    return false;
  }

  public void deleteAllApps(String toolName) {
    try {

      waitForElement(By.className("delete_tool_link"));
      WebElement context =
          driver.findElement(By.xpath("//div[@class='ExternalToolsTable']//table"));
      while (isPresent(By.className("delete_tool_link"))) {
        WebElement deleteToolButton = context.findElement(By.className("delete_tool_link"));
        deleteToolButton.click();
        By dialogDeleteBy = By.xpath("//button[text() = 'Delete']");
        waiter.until(ExpectedConditions.elementToBeClickable(dialogDeleteBy));
        driver.findElement(dialogDeleteBy).click();
        waiter.until(ExpectedConditions2.stalenessOrNonPresenceOf(deleteToolButton));
      }
    } catch (TimeoutException e) {
      // dangerous but there should always be at least one external tool
      // in the canvas instance on startup. Apologies in advance for the
      // inevitable bug this causes
    }
  }
}
