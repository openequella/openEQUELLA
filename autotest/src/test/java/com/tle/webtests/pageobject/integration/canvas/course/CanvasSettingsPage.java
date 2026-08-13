package com.tle.webtests.pageobject.integration.canvas.course;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.integration.canvas.course.settings.CanvasAppsTab;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CanvasSettingsPage extends AbstractCanvasCoursePage<CanvasSettingsPage> {
  @FindBy(xpath = "//li[@id='external_tools_tab']/a")
  private WebElement appsLink;

  public CanvasSettingsPage(PageContext context) {
    super(context, By.id("course_details_tabs"));
  }

  public CanvasAppsTab apps() {
    appsLink.click();
    return new CanvasAppsTab(context).get();
  }
}
