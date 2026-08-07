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

public class MenuSection extends AbstractPage<MenuSection> {

  public MenuSection(PageContext context) {
    super(context);
    loadedBy = isNewUI() ? By.id("menulinks") : By.id("menu");
  }

  private By linkByText(String text) {
    return linkByTextAndHref(text, null);
  }

  private By linkByTextAndHref(String text, String href) {
    String quotedText = quoteXPath(text);
    String hrefXPath = "";
    if (href != null) {
      hrefXPath = " and @href=" + quoteXPath(href);
    }
    if (isNewUI()) {
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

  public <T extends AbstractPage<T>> T clickMenu(String title, T page) {
    WebElement link = findLink(title);
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
    if (hasMenuOption(title)) {
      findLink(title).click();
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

  public boolean hasHierarchyTopic(String title, int position) {
    return isPresent(linkByText(title));
  }

  public boolean waitForCustomIconByNameAndHref(String name, String url) {
    return hasCustomIcon(
        waiter.until(ExpectedConditions.visibilityOfElementLocated(linkByTextAndHref(name, url))));
  }

  public boolean linkExists(String name, String url) {
    List<WebElement> elems = driver.findElements(linkByTextAndHref(name, url));
    return !elems.isEmpty();
  }

  public boolean linkExistsWithIcon(String name, String url, boolean customIcon) {
    List<WebElement> elems = driver.findElements(linkByTextAndHref(name, url));

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
