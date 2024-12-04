package com.tle.webtests.pageobject.integration.blackboard;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class BlackboardToolAvailabilityPage
    extends AbstractBlackboardCoursePage<BlackboardToolAvailabilityPage> {

  public BlackboardToolAvailabilityPage(PageContext context, String courseName) {
    super(context, courseName, "Tool Availability");
  }

  public void availible() {
    waitForElement(By.id("listContainer_databody"));
    WebElement check =
        driver.findElement(
            By.xpath(
                "//label[text()='Available in Content Area - EQUELLA"
                    + " Object']/following-sibling::input"));
    if (!check.isSelected()) {
      check.click();
    }
    driver.findElement(By.name("bottom_Submit")).click();
    waitForElement(By.id("goodMsg1"));
  }
}
