package com.tle.webtests.pageobject.institution;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
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
    // Use Selenide to click the tab link so the click waits for the anchor to become
    // visible/interactable and retries on stale elements. The Sections framework does a full
    // page reload on tab navigation, and on slow/fresh CI machines a raw findElement().click()
    // could fire before the link was ready, silently no-op, and then time out waiting for the
    // destination page to load.
    By tabLink = By.xpath("//a[text()=" + quoteXPath(tab.getTabName()) + "]");
    $(tabLink).shouldBe(Condition.visible, context.getTestConfig().getStandardTimeout()).click();
    return (TA) tab.get();
  }

  @Override
  public String getTabName() {
    return tabName;
  }
}
