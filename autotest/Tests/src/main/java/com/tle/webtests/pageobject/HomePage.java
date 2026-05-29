package com.tle.webtests.pageobject;

import static com.codeborne.selenide.Condition.clickable;
import static com.codeborne.selenide.Selectors.by;
import static com.codeborne.selenide.Selectors.byLinkText;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.SelenideElement;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.portal.AbstractPortalEditPage;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.portal.PortalScreenOptions;
import org.openqa.selenium.By;

public class HomePage extends AbstractPage<HomePage> {
  public HomePage(PageContext context) {
    super(context);
    setLoadedBy(isNewUI());
  }

  public HomePage(PageContext context, boolean isNewUI) {
    super(context);
    setLoadedBy(isNewUI);
  }

  // Set the loadedBy element based on whether it's the new UI or not.
  private void setLoadedBy(boolean isNewUI) {
    String loadedByXpath =
        isNewUI ? "//h5[text()='Dashboard']" : "//div[contains(@class, 'dashboard')]";
    loadedBy = By.xpath(loadedByXpath);
  }

  public boolean portalExists(String title) {
    return isPresent(By.xpath("//h3[normalize-space(text())=" + quoteXPath(title) + "]"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "home.do");
  }

  public boolean containsLink(String name, String url) {
    MenuSection ms = new MenuSection(context).get();
    return ms.linkExists(name, url);
  }

  private PortalScreenOptions openScreenOptions() {
    return new PortalScreenOptions(context).open();
  }

  public <P extends AbstractPortalEditPage<P>> P addPortal(P portal) {
    return new PortalScreenOptions(context).open().addPortal(portal);
  }

  public HomePage restoreAll() {
    openScreenOptions().restoreAll();
    return new HomePage(context).get();
  }

  public boolean isTopicTagVisible(String dynamicTopicName) {
    MenuSection ms = new MenuSection(context).get();
    return ms.hasMenuOption(dynamicTopicName);
  }

  /**
   * Logs out the user by clicking on the "My Account" icon and then selecting "Logout" from the
   * dropdown menu. Users should be navigated to the Login page.
   */
  public LoginPage logout() {
    SelenideElement myAccountIcon = $(by("aria-label", "My Account")).shouldBe(clickable);
    myAccountIcon.click();

    SelenideElement logoutBtn = $(byLinkText("Logout")).shouldBe(clickable);
    logoutBtn.click();

    return new LoginPage(context).get();
  }
}
