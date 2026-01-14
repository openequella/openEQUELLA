package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class RecentContributionsEditPage
    extends AbstractPortalEditPage<RecentContributionsEditPage> {
  private static final String VALUE_ONLY_OPTION = "titleOnly";
  private static final String TITLE_AND_DESCRIPTION_OPTION = "Show the title and description";

  private EquellaSelect statusList;
  private EquellaSelect displayList;

  public RecentContributionsEditPage(PageContext context) {
    super(context);
  }

  @Override
  public void checkLoaded() throws Error {
    super.checkLoaded();
    statusList = new EquellaSelect(context, driver.findElement(By.id("rct_s")));
    displayList = new EquellaSelect(context, driver.findElement(By.id("rct_d")));
  }

  /** Ensures the "All resources" option is selected in the collections filter. */
  public void ensureAllResourcesSelected() {
    WebElement allResourceOption =
        driver.findElement(By.xpath("//input[@id=//label[text()='All resources']/@for]"));
    if (!allResourceOption.isSelected()) {
      allResourceOption.click();
    }
  }

  @Override
  public String getType() {
    return "Recent contributions";
  }

  @Override
  public String getId() {
    return "rct";
  }

  public void setStatus(String status) {
    statusList.selectByValue(status);
  }

  public void setQuery(String query) {
    driver.findElement(By.id("rct_q")).sendKeys(query);
  }

  public void setAge(String age) {
    driver.findElement(By.id("rct_a")).sendKeys(age);
  }

  public void setDisplayTitleOnly(boolean titleOnly) {
    if (titleOnly) {
      displayList.selectByValue(VALUE_ONLY_OPTION);
    }
    // It uses selectByVisibleText because there is no value attribute for "Show the title and
    // description".
    else {
      displayList.selectByVisibleText(TITLE_AND_DESCRIPTION_OPTION);
    }
  }
}
