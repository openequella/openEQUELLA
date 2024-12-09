package com.tle.webtests.pageobject.wizard.controls;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.controls.NavNodeEditor.AddTabDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class NavNodePageObject extends AbstractPage<NavNodePageObject> {
  @FindBy(xpath = ".//span/a")
  private WebElement selectLink;

  @FindBy(xpath = "div[@class='navChildren']")
  private WebElement childrenParent;

  @FindBy(xpath = "div[contains(@class, 'nodeLine')]")
  private WebElement selectElem;

  private NavigationBuilder navBuilder;
  private boolean anyChild;
  private String displayName;

  public NavNodePageObject(NavigationBuilder navBuilder, SearchContext parent, String displayName) {
    super(navBuilder.getContext(), parent, null);
    this.navBuilder = navBuilder;
    this.displayName = displayName;
    refreshBy();
  }

  public void setAnyChild(boolean anyChild) {
    this.anyChild = anyChild;
    refreshBy();
  }

  private void refreshBy() {
    String xpath =
        "div[contains(@class, 'navNode') and"
            + " div/div[@class='container']/span[@class='textBox']/a/text() = "
            + quoteXPath(displayName)
            + "]";
    if (anyChild) {
      xpath = ".//" + xpath;
    }
    loadedBy = By.xpath(xpath);
  }

  @Override
  public SearchContext getSearchContext() {
    return loadedElement;
  }

  public NavNodeEditor select() {
    NavNodeEditor editor = new NavNodeEditor(navBuilder, this);
    if (!selectElem.getAttribute("class").contains("selected")) {
      scrollToElement(selectLink);
      selectLink.click();
    }
    return editor.get();
  }

  public NavNodePageObject newChild() {
    return new NavNodePageObject(navBuilder, childrenParent, "New Node");
  }

  public WaitingPageObject<NavigationBuilder> removalWaiter() {
    return navBuilder.removalWaiter(loadedElement);
  }

  public NavNodePageObject newSibling() {
    return new NavNodePageObject(navBuilder, relativeTo, "New Node");
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
    refreshBy();
    get();
  }

  public void deleteTab(int tabIndex) {
    NavNodeEditor editor = select();
    editor.setMultiple(true);
    driver.findElement(By.id("tab" + tabIndex + "delete")).click();
    acceptConfirmation();
  }

  public void addTab(String resource, String tabName) {
    NavNodeEditor editor = select();
    editor.setMultiple(true);
    AddTabDialog addTab = editor.addTab();
    addTab.setTabName(tabName);
    addTab.setResource(resource);
    addTab.save();
  }

  public String getDisplayName() {
    return displayName;
  }

  public void addTabWithoutResource(String tabName) {
    NavNodeEditor editor = select();
    editor.setMultiple(true);
    AddTabDialog addTab = editor.addTab();
    addTab.setTabName(tabName);
    addTab.save();
  }

  public WaitingPageObject<NavNodePageObject> moveWaiter() {
    return this;
  }
}
