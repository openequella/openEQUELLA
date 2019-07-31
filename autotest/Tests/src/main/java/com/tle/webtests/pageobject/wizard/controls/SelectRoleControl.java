package com.tle.webtests.pageobject.wizard.controls;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.SelectRoleDialog;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class SelectRoleControl extends AbstractWizardControl<SelectRoleControl> {
  private WebElement getSelectRoleButton() {
    return byWizId("_addLink");
  }

  private WebElement getRootElem() {
    return byWizId("roleselector");
  }

  public SelectRoleControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page) {
    super(context, ctrlnum, page);
  }

  @Override
  protected WebElement findLoadedElement() {
    return getSelectRoleButton();
  }

  private String xpathForRolename(String rolename) {
    return ".//tr/td[@class='name' and text()=" + quoteXPath(rolename) + "]";
  }

  public SelectRoleDialog openDialog() {
    getSelectRoleButton().click();
    return new SelectRoleDialog(context, page.subComponentId(ctrlnum, "s")).get();
  }

  public AbstractWizardControlPage<?> queryAndSelect(String query, String rolename) {
    openDialog().search(query).selectAndFinish(rolename, selectedWaiter(rolename));
    return page;
  }

  public void removeRole(String rolename) {
    WaitingPageObject<SelectRoleControl> waiter = removedWaiter(rolename);
    getRootElem()
        .findElement(
            By.xpath(xpathForRolename(rolename) + "/../td[@class='actions']/a[@class='unselect']"))
        .click();
    acceptConfirmation();
    waiter.get();
  }

  public WaitingPageObject<SelectRoleControl> selectedWaiter(String newlySelected) {
    return ExpectWaiter.waiter(
        ExpectedConditions2.visibilityOfElementLocated(
            getRootElem(), By.xpath(xpathForRolename(newlySelected))),
        this);
  }

  public WaitingPageObject<SelectRoleControl> removedWaiter(String removed) {
    return ExpectWaiter.waiter(
        ExpectedConditions2.invisibilityOfElementLocated(
            getRootElem(), By.xpath(xpathForRolename(removed))),
        this);
  }
}
