package com.tle.webtests.pageobject.institution;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public abstract class InstitutionTab<T extends InstitutionTab<T>> extends AbstractPage<T>
    implements InstitutionTabInterface {

  private final String tabName;

  protected InstitutionTab(PageContext context, String tabName, String title) {
    super(context, By.xpath("//h2[normalize-space(text())=" + quoteXPath(title) + "]"));
    this.tabName = tabName;
  }

  public ImportTab importTab() {
    return clickTab(new ImportTab(context));
  }

  public ServerSettingsTab serverSettingsTab() {
    return clickTab(new ServerSettingsTab(context));
  }

  public <TA extends InstitutionTab<TA>> TA clickTab(InstitutionTab<? extends TA> tab) {
    driver.findElement(By.xpath("//a[text()=" + quoteXPath(tab.getTabName()) + "]")).click();
    return (TA) tab.get();
  }

  @Override
  public String getTabName() {
    return tabName;
  }
}
