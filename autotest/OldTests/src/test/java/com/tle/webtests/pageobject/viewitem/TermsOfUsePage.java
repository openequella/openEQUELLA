package com.tle.webtests.pageobject.viewitem;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class TermsOfUsePage extends AbstractPage<TermsOfUsePage> {

  public TermsOfUsePage(PageContext context) {
    super(context, By.xpath("//h2[text()='Terms of use']"));
  }

  public boolean hasTerms(String terms) {
    try {
      WebElement termsDiv = driver.findElement(By.className("terms"));
      return termsDiv.getText().equals(terms);
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  public int getNumberOfAcceptances() {
    return Integer.parseInt(driver.findElement(By.id("acceptancecount")).getText().split(" ")[0]);
  }

  public List<String> getUsersAccepted() {
    List<WebElement> cells = driver.findElements(By.xpath("//div/table/tbody/tr/td[1]"));
    return Lists.transform(
        cells,
        new Function<WebElement, String>() {
          @Override
          public String apply(WebElement we) {
            return we.getText();
          }
        });
  }
}
