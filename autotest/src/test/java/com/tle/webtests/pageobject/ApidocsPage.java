package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ApidocsPage extends AbstractPage<ApidocsPage> {

  private final boolean forceOld;

  public static String getUrl() {
    return "apidocs.do";
  }

  public String getFullUrl() {
    return context.getBaseUrl() + ApidocsPage.getUrl();
  }

  public ApidocsPage(PageContext context) {
    this(context, false);
  }

  public ApidocsPage(PageContext context, boolean forceOld) {
    super(context); //  /h2[normalize-space(text())='An error occurred']
    this.forceOld = forceOld;
  }

  @Override
  protected void loadUrl() {
    driver.get(getFullUrl());
  }

  @Override
  protected WebElement findLoadedElement() {
    return driver.findElement(By.id("swagger-ui"));
  }

  protected boolean isNewUI() {
    return !forceOld && super.isNewUI();
  }

  public String getMainHeader() {
    return driver.findElement(By.xpath("//div[@class='area']/h2")).getText();
  }
}
