package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.UndeterminedPage;
import com.tle.webtests.pageobject.hierarchy.TopicPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;

// TODO: remove MenuSection, linkByText, linkByTextAndHref, hasMenuOption, hasHierarchyTopic in
// OEQ-1702.
public class MenuSection extends AbstractPage<MenuSection> {

  public MenuSection(PageContext context) {
    super(context);
    loadedBy = isNewUI() ? By.id("menulinks") : By.id("menu");
  }

  // TODO: Remove this method OEQ-1702.
  // Specific for hierarchy menu related test because the new UI hasn't been completed yet.
  public MenuSection(PageContext context, Boolean isNewUI) {
    super(context);
    loadedBy = isNewUI ? By.id("menulinks") : By.id("menu");
  }

  private By linkByText(String text) {
    return linkByTextAndHref(text, null, false);
  }

  // TODO: Remove this method in OEQ-1702.
  // Specific for hierarchy menu related test because the new UI hasn't been completed yet.
  private By linkByText(String text, boolean forceOldUi) {
    return linkByTextAndHref(text, null, forceOldUi);
  }

  // TODO: Remove forceOldUi param in OEQ-1702.
  // Add forceOldUi param for hierarchy menu related test because the new UI hasn't been completed
  // yet.
  private By linkByTextAndHref(String text, String href, boolean forceOldUi) {
    String quotedText = quoteXPath(text);
    String hrefXPath = "";
    if (href != null) {
      hrefXPath = " and @href=" + quoteXPath(href);
    }
    if (!forceOldUi && isNewUI()) {
      return By.xpath("id('menulinks')//a[./div/div[text()=" + quotedText + "]" + hrefXPath + "]");
    }
    return By.xpath("id('menu')//a[text()=" + quotedText + hrefXPath + "]");
  }

  private By linkByHref(String href) {
    return new ByChained(loadedBy, By.xpath(".//a[@href=" + quoteXPath(href) + "]"));
  }

  private WebElement findLink(String title) {
    By textLink = linkByText(title);
    waiter.until(ExpectedConditions.visibilityOfElementLocated(textLink));
    return driver.findElement(textLink);
  }

  // TODO: Remove this method in OEQ-1702.
  // Specific for hierarchy menu related test because the new UI hasn't been completed yet.
  private WebElement findLink(String title, boolean forceOldUi) {
    By textLink = linkByText(title, forceOldUi);
    waiter.until(ExpectedConditions.visibilityOfElementLocated(textLink));
    return driver.findElement(textLink);
  }

  public <T extends AbstractPage<T>> T clickMenu(String title, T page) {
    WebElement link = findLink(title);
    page.getWaiter().until(ExpectedConditions.elementToBeClickable(link));
    link.click();
    return page.get();
  }

  // TODO: Remove this method in OEQ-1702
  // Specific for hierarchy menu related test because the new UI hasn't been completed yet.
  public <T extends AbstractPage<T>> T clickMenuForceOldUI(String title, T page) {
    WebElement link = findLink(title, true);
    page.getWaiter().until(ExpectedConditions.elementToBeClickable(link));
    link.click();
    return page.get();
  }

  // Handles the case of a single wizard...
  public WizardPageTab clickContribute(String wizardName) {
    findLink("Contribute").click();
    WizardPageTab wpt = new WizardPageTab(context, 0);
    ContributePage cp = new ContributePage(context);
    UndeterminedPage<PageObject> unknown = new UndeterminedPage<PageObject>(context, cp, wpt);

    PageObject po = unknown.get();

    if (po == cp) {
      return cp.openWizard(wizardName);
    }

    return wpt;
  }

  public <T extends AbstractPage<T>> T clickMenuLink(String href, T page) {
    driver.findElement(linkByHref(href)).click();
    return page.get();
  }

  public TopicPage clickTopic(String title) {
    if (hasMenuOption(title, true)) {
      findLink(title, true).click();
      return new TopicPage(context, title).get();
    } else {
      return new TopicPage(context, "Browse").load().clickSubTopic(title);
    }
  }

  public HomePage home() {
    return clickMenu("Dashboard", new HomePage(context));
  }

  public boolean hasMenuOption(String title) {
    return isPresent(linkByText(title));
  }

  // TODO: Remove this method in OEQ-1702.
  // Specific for hierarchy menu related test because the new UI hasn't been completed yet.
  public boolean hasMenuOption(String title, Boolean forceOldUi) {
    return isPresent(linkByText(title, forceOldUi));
  }

  public boolean hasHierarchyTopic(String title, int position) {
    return isPresent(linkByText(title));
  }

  // TODO: Remove this method in OEQ-1702.
  // Specific for hierarchy menu related test because the new UI hasn't been completed yet.
  public boolean hasHierarchyTopic(String title, int position, Boolean forceOldUi) {
    return isPresent(linkByText(title, forceOldUi));
  }

  public boolean waitForCustomIconByNameAndHref(String name, String url) {
    return hasCustomIcon(
        waiter.until(
            ExpectedConditions.visibilityOfElementLocated(linkByTextAndHref(name, url, false))));
  }

  public boolean linkExists(String name, String url) {
    List<WebElement> elems = driver.findElements(linkByTextAndHref(name, url, false));
    return !elems.isEmpty();
  }

  public boolean linkExistsWithIcon(String name, String url, boolean customIcon) {
    List<WebElement> elems = driver.findElements(linkByTextAndHref(name, url, false));

    if (elems.isEmpty()) {
      return false;
    }
    return hasCustomIcon(elems.get(0)) == customIcon;
  }

  private boolean hasCustomIcon(WebElement menuItem) {
    return isNewUI()
        ? !menuItem.findElements(By.xpath("./div/img")).isEmpty()
        : menuItem.getAttribute("style").startsWith("background-image");
  }
}
