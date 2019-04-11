package com.tle.webtests.pageobject.wizard.controls;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class NavigationBuilder extends AbstractWizardControl<NavigationBuilder> {
  private WebElement getInitButton() {
    return byWizId("_pp");
  }

  private WebElement getAddChildButton() {
    return byWizId("_ac");
  }

  private WebElement getAddSiblingButton() {
    return byWizId("_as");
  }

  private WebElement getRemoveButton() {
    return byWizId("_r");
  }

  private WebElement getMoveUpButton() {
    return byWizId("_mu");
  }

  private WebElement getMoveDownButton() {
    return byWizId("_md");
  }

  private WebElement getShowOthersCheck() {
    return byWizId("_mn");
  }

  private WebElement getShowSplitCheck() {
    return byWizId("_ss");
  }

  @FindBy(id = "root")
  private WebElement rootDiv;

  public NavigationBuilder(PageContext context, AbstractWizardControlPage<?> page) {
    super(context, 1, page);
  }

  @Override
  protected WebElement findLoadedElement() {
    return getInitButton();
  }

  public NavigationBuilder initialiseNavigation(boolean wasBlank) {
    WaitingPageObject<NavigationBuilder> waiter = updateWaiter();
    getInitButton().click();
    if (!wasBlank) {
      acceptConfirmation();
    }
    return waiter.get();
  }

  public int nodeCount() {
    return rootDiv.findElements(By.xpath(".//div[@class='navNode']")).size();
  }

  public boolean nodeExists(String title) {
    return isPresent(rootDiv, By.xpath(".//a[text()=" + quoteXPath(title) + "]"));
  }

  private NavNodePageObject selectNode(String title) {
    NavNodePageObject navNode = new NavNodePageObject(this, rootDiv, title);
    navNode.setAnyChild(true);
    navNode = navNode.get();
    navNode.select();
    return navNode;
  }

  public void removeNode(String title) {
    NavNodePageObject node = selectNode(title);
    WaitingPageObject<NavigationBuilder> removalWaiter = node.removalWaiter();
    getRemoveButton().click();
    removalWaiter.get();
  }

  public NavNodePageObject addChild(String target, String newTitle, String resourceName) {
    return addChild(target, newTitle, resourceName, "");
  }

  public NavNodePageObject addChild(
      String target, String newTitle, String resourceName, String viewerName) {
    NavNodePageObject parentNode = selectNode(target);
    NavNodePageObject newChild = parentNode.newChild();
    getAddChildButton().click();
    newChild.get().select().setFields(newTitle, resourceName, viewerName);
    return newChild;
  }

  public NavNodePageObject addSibling(String target, String newTitle, String resourceName) {
    return addSibling(target, newTitle, resourceName, "");
  }

  public NavNodePageObject addSibling(
      String target, String newTitle, String resourceName, String viewerName) {
    NavNodePageObject sibling = selectNode(target);
    NavNodePageObject newSibling = sibling.newSibling();
    getAddSiblingButton().click();
    newSibling.get().select().setFields(newTitle, resourceName, viewerName);
    return newSibling;
  }

  public NavNodePageObject addTopLevelNode(String displayName, String resourceName) {
    NavNodePageObject newSibling = new NavNodePageObject(this, rootDiv, "New Node");
    getAddSiblingButton().click();
    newSibling.get().select().setFields(displayName, resourceName, "");
    return newSibling;
  }

  public void moveUp(String target) {
    NavNodePageObject navNode = selectNode(target);
    WaitingPageObject<NavNodePageObject> waiter = navNode.moveWaiter();
    getMoveUpButton().click();
    waiter.get();
  }

  public void moveDown(String target) {
    NavNodePageObject navNode = selectNode(target);
    WaitingPageObject<NavNodePageObject> waiter = navNode.moveWaiter();
    getMoveDownButton().click();
    waiter.get();
  }

  public void dragToBefore(String moveThisNode, String beforeNode) {
    // FIXME when we can drag again

    // String xp =
    // ".//div[contains(@class, 'droppable') and contains(@class, 'before')]";
    // if( Check.isEmpty(beforeNode) )
    // {
    // xp += "[0]";
    // }
    // else
    // {
    // xp += "[following-sibling::div/div/span/a[text()=" +
    // quoteXPath(beforeNode) + "]]";
    // }

    // new Actions(driver).dragAndDrop(getNodeElement(moveThisNode),
    // rootDiv.findElement(By.xpath(xp))).perform();
  }

  public void setShowOtherAttachments(boolean check) {
    if (getShowOthersCheck().isSelected() != check) {
      getShowOthersCheck().click();
    }
  }

  public void setSplitView(boolean check) {
    if (getShowSplitCheck().isSelected() != check) {
      getShowSplitCheck().click();
    }
  }
}
