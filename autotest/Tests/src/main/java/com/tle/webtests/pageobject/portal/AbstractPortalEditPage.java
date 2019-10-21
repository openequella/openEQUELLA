package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.MultiLingualEditbox;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractPortalEditPage<T extends AbstractPortalEditPage<T>>
    extends AbstractPage<T> {
  private WebElement getDisableCheck() {
    return findById("_d");
  }

  private WebElement getSave() {
    return findById("_sv");
  }

  private WebElement getOthersCheck() {
    return findById("_i");
  }

  private WebElement getMinCheck() {
    return findById("_m");
  }

  private By getCloseCheckBy() {
    return byPrefixId(getId(), "_c");
  }

  private WebElement getCloseCheck() {
    return find(driver, getCloseCheckBy());
  }

  private WebElement findById(String postfix) {
    return driver.findElement(By.id(getId() + postfix));
  }

  private By getViewExpressionInput() {
    return By.name(getId() + "_selector_es.e");
  }

  public AbstractPortalEditPage(PageContext context) {
    super(context, By.xpath("//div[normalize-space(@class)='portletedit']"));
  }

  public AbstractPortalEditPage(PageContext context, By loadedBy) {
    super(context, loadedBy);
  }

  public <P extends AbstractPage<P>> P save(P page) {
    WebElement saveButton = getSave();
    waiter.until(ExpectedConditions.elementToBeClickable(saveButton));
    saveButton.click();
    waiter.until(ExpectedConditions.visibilityOf(page.getLoadedElement()));
    return page.get();
  }

  public T setTitle(String title) {
    getTitleSection().setCurrentString(title);
    return get();
  }

  public T setTitle(PrefixedName title) {
    getTitleSection().setCurrentString(title.toString());
    return get();
  }

  public MultiLingualEditbox getTitleSection() {
    return new MultiLingualEditbox(context, getId() + "_t").get();
  }

  private void check(WebElement check, boolean checked) {
    if (check.isSelected() != checked) {
      check.click();
    }
  }

  public T setDisabled(boolean checked) {
    check(getDisableCheck(), checked);
    return get();
  }

  public T setShowForOthers(boolean checked) {
    if (getOthersCheck().isSelected() != checked) {
      WaitingPageObject<T> aWaiter;
      if (checked) {
        aWaiter = visibilityWaiter(driver, getCloseCheckBy());
      } else {
        aWaiter = removalWaiter(getCloseCheck());
      }

      getOthersCheck().click();
      return aWaiter.get();
    }
    return get();
  }

  public T setUsersCanClose(boolean checked) {
    setShowForOthers(true);
    check(getCloseCheck(), checked);
    return get();
  }

  public T setUsersCanMin(boolean checked) {
    setShowForOthers(true);
    check(getMinCheck(), checked);
    return get();
  }

  public T showForAll() {
    return showForExpression("* ");
  }

  public T showForExpression(String expression) {

    setShowForOthers(true);
    executeSubmit("('" + getId() + ".expression', '', '" + expression + "');");
    return ExpectWaiter.waiter(
            ExpectedConditions.attributeToBe(getViewExpressionInput(), "value", expression), this)
        .get();
  }

  public abstract String getType();

  public abstract String getId();
}
