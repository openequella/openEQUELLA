package com.tle.webtests.pageobject.integration.blackboard;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class BlackboardBulkDeletePage
    extends AbstractBlackboardCoursePage<BlackboardBulkDeletePage> {

  public BlackboardBulkDeletePage(PageContext context, String courseName) {
    super(context, courseName, "Bulk Delete");
  }

  public void delete() {
    waitForElement(By.id("dataCollectionContainer"));
    driver.findElement(By.xpath("//label[text()='Information']/following-sibling::input")).click();
    driver.findElement(By.xpath("//label[text()='Content']/following-sibling::input")).click();
    attemptDelete();
    new BlackboardUtillitiesPage(context, courseName).get();
  }

  // Errgh dirty chrome input truncation hackery test
  private void attemptDelete() {
    int tries = 0;
    WebElement conf = driver.findElement(By.id("confirmation"));
    while (!conf.getAttribute("value").equals("Delete") && tries <= 10) {
      conf.clear();
      conf.sendKeys("Delete");
      conf = driver.findElement(By.id("confirmation"));
      tries++;
    }
    driver.findElement(By.xpath("//input[@type='submit']")).click();
  }
}
