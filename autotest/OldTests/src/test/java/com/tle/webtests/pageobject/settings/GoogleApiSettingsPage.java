package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public class GoogleApiSettingsPage extends AbstractPage<GoogleApiSettingsPage> {
  public GoogleApiSettingsPage(PageContext context) {
    super(context, By.xpath("//h2[text()='Google API']"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/googleapisettings.do");
  }
}
