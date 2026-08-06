package com.tle.webtests.pageobject.wizard.controls;

import com.tle.common.Check;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class NavNodeEditor extends NewAbstractWizardControl<NavNodeEditor> {
  private WebElement getSingleResource() {
    return byWizId("optsingle");
  }

  private WebElement getMultipleResource() {
    return byWizId("optmultiple");
  }

  private WebElement getResource() {
    return byWizId("_al");
  }

  private WebElement getViewer() {
    return byWizId("_vl");
  }

  private WebElement getNameField() {
    return byWizId("_nd");
  }

  private WebElement getAddResourceButton() {
    return byWizId("_ta");
  }

  @FindBy(id = "tabs")
  private WebElement tabsElem;

  private final NavNodePageObject navNode;

  public NavNodeEditor(NavigationBuilder builder, NavNodePageObject navNode) {
    super(builder.getContext(), builder.getCtrlNum(), builder.getPage());
    this.navNode = navNode;
  }

  @Override
  protected WebElement findLoadedElement() {
    return getNameField();
  }

  @Override
  protected void checkLoadedElement() {
    super.checkLoadedElement();
    if (!loadedElement.getAttribute("value").equals(navNode.getDisplayName())) {
      throw new Error("Name field not updated");
    }
  }

  public void setFields(String newTitle, String resourceName, String viewerName) {
    if (!Check.isEmpty(newTitle)) {
      getNameField().clear();
      getNameField().sendKeys(newTitle);
      navNode.setDisplayName(newTitle);
    }
    if (!Check.isEmpty(resourceName)) {
      new EquellaSelect(context, getResource()).selectByVisibleText(resourceName);
    }
    if (!Check.isEmpty(viewerName)) {
      new EquellaSelect(context, getViewer()).selectByVisibleText(viewerName);
    }
  }

  public void setMultiple(boolean multiple) {
    if (getMultipleResource().isSelected() != multiple) {
      ExpectWaiter<NavNodeEditor> waiter;
      if (multiple) {
        waiter = ExpectWaiter.waiter(ExpectedConditions.visibilityOf(tabsElem), this);
        getMultipleResource().click();
      } else {
        waiter = ExpectWaiter.waiter(ExpectedConditions2.invisibilityOf(tabsElem), this);
        getSingleResource().click();
      }
      waiter.get();
    }
  }

  public AddTabDialog addTab() {
    getAddResourceButton().click();
    return new AddTabDialog(this).get();
  }

  public static class AddTabDialog extends NewAbstractWizardControl<AddTabDialog> {
    private WebElement getTabResource() {
      return byWizId("_tabDialog_tal");
    }

    private WebElement getTabViewer() {
      return byWizId("_tabDialog_tvl");
    }

    private WebElement getTabNameField() {
      return byWizId("_tabDialog_ptn");
    }

    private WebElement getTabSave() {
      return byWizId("_tabDialog_ok");
    }

    private final NavNodeEditor navEditor;

    public AddTabDialog(NavNodeEditor navEditor) {
      super(navEditor.getContext(), navEditor.getCtrlNum(), navEditor.getPage());
      this.navEditor = navEditor;
    }

    @Override
    protected WebElement findLoadedElement() {
      return getTabNameField();
    }

    public void setResource(String resource) {
      new EquellaSelect(context, getTabResource()).selectByVisibleText(resource);
    }

    public void setTabName(String tabName) {
      getTabNameField().clear();
      getTabNameField().sendKeys(tabName);
    }

    public NavNodeEditor save() {
      WaitingPageObject<NavNodeEditor> waiter =
          navEditor.getTabWaiter(getTabNameField().getAttribute("value"));
      getTabSave().click();
      return waiter.get();
    }
  }

  public WaitingPageObject<NavNodeEditor> getTabWaiter(String tabName) {
    return ExpectWaiter.waiter(
        ExpectedConditions2.visibilityOfElementLocated(
            tabsElem, By.xpath("li[a/text()=" + quoteXPath(tabName) + "]")),
        this);
  }
}
