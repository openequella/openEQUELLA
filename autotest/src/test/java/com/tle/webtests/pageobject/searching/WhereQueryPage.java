/** */
package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class WhereQueryPage<T extends PageObject>
    extends AbstractWizardControlPage<WhereQueryPage<T>> {
  public static final String QUERY_DIV_ID = "iaw_div";
  public static final String PREDICATE_OPTION = "iaw_whereStart";
  public static final String WHERE_PATH = "iaw_wherePath";
  public static final String WHERE_OPERAND = "iaw_whereOperator";
  public static final String WHERE_VALUE = "iaw_whereValue";
  public static final String ADD_CITERION = "iaw_add";
  public static final String SEARCH_BUTTON = "iaw_search";
  public static final By REMOVE_BUTTONS = By.xpath("id('iaw_div')//span[@class='criteria-remove']");
  private WaitingPageObject<T> parentPage;

  public WhereQueryPage(PageContext context, WaitingPageObject<T> parentPage) {
    super(context, By.id(QUERY_DIV_ID), 0);
    this.parentPage = parentPage;
  }

  private WebElement getQueryDiv() {
    return context.getDriver().findElement(By.id(QUERY_DIV_ID));
  }

  public void clearCriteria() {
    waiter.until(
        ExpectedConditions.or(
            ExpectedConditions.numberOfElementsToBe(REMOVE_BUTTONS, 0),
            new ExpectedCondition<Void>() {

              @Override
              public Void apply(WebDriver t) {
                List<WebElement> removes = t.findElements(REMOVE_BUTTONS);
                if (removes.size() > 0) {
                  removes.get(0).click();
                }
                return null;
              }
            }));
  }

  /**
   * "WHERE" is probably the only clause available on initial construction of the query. "AND" or
   * "OR" can be expected to appear after a "WHERE" exists.
   *
   * @param predicate
   */
  public void setPredicate(String predicate) {
    WebElement selectedClause = getQueryDiv().findElement(By.id(PREDICATE_OPTION));
    EquellaSelect predicateOptions = new EquellaSelect(context, selectedClause);
    predicateOptions.selectByVisibleText(predicate);
  }

  public void setWherePath(String wherePath) {
    WebElement wherePathInput = getQueryDiv().findElement(By.id(WHERE_PATH));
    wherePathInput.sendKeys(wherePath);
  }

  public void setWhereOperand(String operand) {
    WebElement defaultSelectedElem = getQueryDiv().findElement(By.id(WHERE_OPERAND));
    EquellaSelect operatorOptions = null;
    operatorOptions = new EquellaSelect(context, defaultSelectedElem);
    operatorOptions.selectByVisibleText(operand);
  }

  public void setWhereValue(String value) {
    WebElement whereValueInput = getQueryDiv().findElement(By.id(WHERE_VALUE));
    whereValueInput.sendKeys(value);
  }

  public void addCriterion() {
    WebElement addCriterionButton = getQueryDiv().findElement(By.id(ADD_CITERION));
    addCriterionButton.click();
  }

  public void execute() {
    WebElement searchButton = context.getDriver().findElement(By.id(SEARCH_BUTTON));
    searchButton.click();
  }

  public T executeCriterion() {
    this.addCriterion();
    this.execute();
    return parentPage.get();
  }

  /**
   * Basically a NO-OP: we don't expect to use this.
   *
   * @see com.tle.webtests.wizard.page.AbstractWizardControlPage#getControlId(int)
   */
  @Override
  public String getControlId(int ctrlNum) {
    return Integer.toString(super.getControlNum(ctrlNum));
  }
}
