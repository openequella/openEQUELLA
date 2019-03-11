package com.tle.webtests.pageobject.integration.blackboard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class BlackboardMoveCopyItemPage extends AbstractPage<BlackboardMoveCopyItemPage> {
  private Select courseSelect;

  @FindBy(xpath = "//input[@value='Browse...']")
  private WebElement browseButton;

  @FindBy(name = "bottom_Submit")
  private WebElement submitButton;

  private final BlackboardContentPage content;

  public BlackboardMoveCopyItemPage(
      PageContext context, BlackboardContentPage content, String type) {
    super(context, BlackboardPageUtils.pageTitleBy(type));
    this.content = content;
  }

  @Override
  public void checkLoaded() throws Error {
    super.checkLoaded();
    courseSelect = new Select(driver.findElement(By.id("destCourse")));
  }

  public void setCourse(String course) {
    courseSelect.selectByVisibleText(course);
  }

  public void setDestination(String destination) {
    String main = driver.getWindowHandle();
    browseButton.click();
    driver.switchTo().window("picker_browse");
    waitForElement(By.linkText(destination));
    driver.findElement(By.linkText(destination)).click();

    driver.switchTo().window(main);
    waitForElement(submitButton);
  }

  public BlackboardContentPage submit() {
    submitButton.click();
    return content.get();
  }
}
