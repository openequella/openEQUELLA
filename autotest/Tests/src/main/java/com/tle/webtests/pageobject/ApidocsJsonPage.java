package com.tle.webtests.pageobject;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ApidocsJsonPage extends AbstractPage<ApidocsJsonPage> {

  private final boolean forceOld;

  public static String getUrl() {
    return "api/swagger.json";
  }

  public ApidocsJsonPage(PageContext context) {
    this(context, false);
  }

  public ApidocsJsonPage(PageContext context, boolean forceOld) {
    super(context);
    this.forceOld = forceOld;
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + ApidocsJsonPage.getUrl());
  }

  @Override
  protected WebElement findLoadedElement() {
    return driver.findElement(By.xpath("//pre"));
  }

  protected boolean isNewUI() {
    return !forceOld && super.isNewUI();
  }
}
