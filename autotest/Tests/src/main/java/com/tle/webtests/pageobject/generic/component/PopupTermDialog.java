package com.tle.webtests.pageobject.generic.component;

import com.tle.webtests.framework.EBy;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class PopupTermDialog extends AbstractPage<PopupTermDialog> {
  private final String baseId;
  private final int ctrlNum;
  private final AbstractWizardControlPage<?> ctrlPage;

  private WebElement getDialog() {
    return byBaseId("");
  }

  private WebElement getQueryField() {
    return byBaseId("_searchQuery");
  }

  private WebElement getSearchButton() {
    return byBaseId("_searchButton");
  }

  private WebElement getOkButton() {
    return byBaseId("_ok");
  }

  private WebElement getTreeTag() {
    return byBaseId("_treeView");
  }

  public PopupTermDialog(
      PageContext context, String baseId, AbstractWizardControlPage<?> ctrlPage, int ctrlNum) {
    super(context);
    this.ctrlPage = ctrlPage;
    this.ctrlNum = ctrlNum;
    this.baseId = baseId;
  }

  @Override
  protected void checkLoadedElement() {
    ensureVisible(getOkButton());
  }

  public WebElement byBaseId(String postfix) {
    return driver.findElement(By.id(baseId + postfix));
  }

  public PopupTermDialog search(String query, int result) {
    WaitingPageObject<PopupTermDialog> visibilityWaiter = visibilityWaiter(getQueryField());
    switchTab("Search Terms");
    visibilityWaiter.get();
    getQueryField().clear();
    getQueryField().sendKeys(query);

    visibilityWaiter = visibilityWaiter(getDialog(), By.xpath(".//div[@class='resultcount']"));
    getSearchButton().click();
    visibilityWaiter.get();

    String term =
        getDialog()
            .findElement(By.xpath("id('searchResults')/ul/li[" + result + "]"))
            .getText()
            .replaceAll("Select   View", "")
            .trim();

    visibilityWaiter =
        visibilityWaiter(
            getDialog(), By.xpath(".//td[@class='name' and text()=" + quoteXPath(term) + "]"));
    getDialog().findElement(By.xpath("id('searchResults')/ul/li[" + result + "]/a")).click();
    return visibilityWaiter.get();
  }

  public PopupTermDialog selectTerm(String term) {
    switchTab("Browse Terms");

    TermEntry lastEntry = ensureTermsVisible(term);
    lastEntry.view();

    WaitingPageObject<PopupTermDialog> visibilityWaiter =
        visibilityWaiter(
            getDialog(),
            By.xpath(".//td[@class='name' and text()=" + quoteXPath(lastEntry.getName()) + "]"));
    getDialog().findElement(EBy.buttonText("Select this term")).click();

    return visibilityWaiter.get();
  }

  private TermEntry ensureTermsVisible(String fullPath) {
    TermEntry lastEntry = null;
    String[] values = fullPath.split("\\\\");
    if (values.length == 1) {
      return new TermEntry(getTreeTag(), values[0]);
    }
    WebElement treeTag = getTreeTag();
    for (int i = 0; i < values.length - 1; i++) {
      TermEntry entry = new TermEntry(treeTag, values[i]);
      entry.expand();
      lastEntry = new TermEntry(entry.getElement(), values[i + 1]);
      lastEntry.getElement();
      treeTag = entry.getElement();
    }
    return lastEntry;
  }

  public void switchTab(String tab) {
    getDialog().findElement(By.xpath(".//span[text()=" + quoteXPath(tab) + "]")).click();
    visibilityWaiter(
            getDialog(),
            By.xpath(
                ".//span[text()="
                    + quoteXPath(tab)
                    + "]/ancestor::li[contains(@class,'ui-tabs-active')]"))
        .get();
  }

  public <T extends PageObject> T finish(WaitingPageObject<T> page) {
    WaitingPageObject<?> updateWaiter = ctrlPage.getUpdateWaiter(ctrlNum);
    getOkButton().click();
    updateWaiter.get();
    return page.get();
  }

  public class TermEntry {
    private WebElement element;
    private SearchContext parent;
    private By childBy;
    private String name;

    public TermEntry(SearchContext parent, String name) {
      this.parent = parent;
      childBy = By.xpath(".//li[starts-with(span/text(), " + quoteXPath(name) + ")]");
      this.name = name;
    }

    public WebElement getElement() {
      if (element == null) {
        element = waitForElement(parent, childBy);
      }
      return element;
    }

    public void view() {
      By xpath =
          By.xpath(
              ".//h3[contains(@class, 'termViewerTerm') and normalize-space(text())="
                  + quoteXPath(name)
                  + "]");
      WaitingPageObject<PopupTermDialog> visibilityWaiter = visibilityWaiter(getDialog(), xpath);
      getElement().findElement(By.xpath("span/a[text()='View']")).click();
      visibilityWaiter.get();
    }

    public void expand() {
      final WebElement span = getElement().findElement(By.xpath("span"));
      final WebElement expandSpan =
          span.findElement(By.xpath("parent::li[contains(@class,'expandable')]"));
      span.click();
      waiter.until(
          new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
              boolean expanded = !expandSpan.getAttribute("class").contains("expandable");
              return expanded && !isVisible(span, By.className("placeholder"));
            }
          });
    }

    public String getName() {
      return name;
    }
  }
}
